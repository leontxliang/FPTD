"""
边缘服务器
"""

import socket
import pickle
import threading
from typing import List, Optional
from .params import Params
from .share import Share


class EdgeServer:
    """边缘服务器 - 处理参与方之间的通信"""
    
    def __init__(self, party_idx: int, is_king: bool = False):
        """
        Args:
            party_idx: 参与方索引
            is_king: 是否为King服务器
        """
        self.party_idx = party_idx
        self.is_king = is_king
        
        # 网络连接
        self.server_socket: Optional[socket.socket] = None
        self.client_sockets: List[socket.socket] = []
        self.king_socket: Optional[socket.socket] = None
        
        # 离线数据读取器
        self.offline_reader = None
    
    def connect_other_servers(self):
        """建立与其他服务器的连接"""
        if self.is_king:
            # King服务器监听连接
            self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.server_socket.bind((Params.HOST, Params.BASE_PORT))
            self.server_socket.listen(Params.NUM_SERVER - 1)
            
            if Params.IS_PRINT_EXE_INFO:
                print(f"King server listening on port {Params.BASE_PORT}")
            
            # 接受所有其他服务器的连接
            for _ in range(Params.NUM_SERVER - 1):
                client_socket, addr = self.server_socket.accept()
                self.client_sockets.append(client_socket)
                if Params.IS_PRINT_EXE_INFO:
                    print(f"Accepted connection from {addr}")
        else:
            # 非King服务器连接到King
            self.king_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.king_socket.connect((Params.HOST, Params.BASE_PORT))
            if Params.IS_PRINT_EXE_INFO:
                print(f"Connected to King server")
    
    def close_connections(self):
        """关闭所有连接"""
        if self.server_socket:
            self.server_socket.close()
        for sock in self.client_sockets:
            sock.close()
        if self.king_socket:
            self.king_socket.close()
    
    # ==================== 通信方法 ====================
    
    def send_to_king(self, data: List[int]):
        """向King发送数据"""
        if self.is_king:
            return
        self._send_data(self.king_socket, data)
    
    def read_from_king(self, size: int) -> List[int]:
        """从King接收数据"""
        if self.is_king:
            return []
        return self._recv_data(self.king_socket)
    
    def king_read_from_all(self, size: int) -> List[List[int]]:
        """King从所有服务器读取数据"""
        if not self.is_king:
            return []
        
        all_data = []
        for sock in self.client_sockets:
            data = self._recv_data(sock)
            all_data.append(data)
        return all_data
    
    def king_send_to_all(self, data: List[int]):
        """King向所有服务器发送数据"""
        if not self.is_king:
            return
        
        for sock in self.client_sockets:
            self._send_data(sock, data)
    
    def send_shares_to_king(self, shares: List[Share]):
        """向King发送份额"""
        data = [s.shr for s in shares]
        self.send_to_king(data)
    
    def king_read_shares_from_all(self, size: int) -> List[List[Share]]:
        """King从所有服务器读取份额"""
        if not self.is_king:
            return []
        
        all_shares = []
        for i, sock in enumerate(self.client_sockets):
            data = self._recv_data(sock)
            shares = [Share(i + 1, val) for val in data]  # party_id从1开始(非King)
            all_shares.append(shares)
        
        # 添加King自己的份额 (需要从外部设置)
        return all_shares
    
    def _send_data(self, sock: socket.socket, data: List[int]):
        """发送数据"""
        serialized = pickle.dumps(data)
        # 先发送长度
        sock.sendall(len(serialized).to_bytes(8, 'big'))
        # 再发送数据
        sock.sendall(serialized)
    
    def _recv_data(self, sock: socket.socket) -> List[int]:
        """接收数据"""
        # 先接收长度
        length_bytes = self._recv_exact(sock, 8)
        length = int.from_bytes(length_bytes, 'big')
        # 再接收数据
        data_bytes = self._recv_exact(sock, length)
        return pickle.loads(data_bytes)
    
    def _recv_exact(self, sock: socket.socket, n: int) -> bytes:
        """精确接收n个字节"""
        data = b''
        while len(data) < n:
            chunk = sock.recv(n - len(data))
            if not chunk:
                raise ConnectionError("Connection closed")
            data += chunk
        return data


class MockEdgeServer(EdgeServer):
    """模拟边缘服务器 - 用于单机测试"""
    
    def __init__(self, party_idx: int, is_king: bool = False):
        super().__init__(party_idx, is_king)
        self.message_queues: dict = {}  # 共享的消息队列
        self.lock = threading.Lock()
    
    def set_shared_queues(self, queues: dict):
        """设置共享的消息队列"""
        self.message_queues = queues
    
    def connect_other_servers(self):
        """模拟连接 (不需要实际网络)"""
        pass
    
    def close_connections(self):
        """模拟关闭连接"""
        pass
    
    def send_to_king(self, data: List[int]):
        """向King发送数据 (通过队列)"""
        if self.is_king:
            return
        queue_key = f"to_king_{self.party_idx}"
        with self.lock:
            if queue_key not in self.message_queues:
                self.message_queues[queue_key] = []
            self.message_queues[queue_key].append(data)
    
    def read_from_king(self, size: int) -> List[int]:
        """从King接收数据 (通过队列)"""
        if self.is_king:
            return []
        queue_key = f"from_king_{self.party_idx}"
        while True:
            with self.lock:
                if queue_key in self.message_queues and self.message_queues[queue_key]:
                    return self.message_queues[queue_key].pop(0)
    
    def king_read_from_all(self, size: int) -> List[List[int]]:
        """King从所有服务器读取数据"""
        if not self.is_king:
            return []
        
        all_data = []
        for i in range(1, Params.NUM_SERVER):
            queue_key = f"to_king_{i}"
            while True:
                with self.lock:
                    if queue_key in self.message_queues and self.message_queues[queue_key]:
                        all_data.append(self.message_queues[queue_key].pop(0))
                        break
        return all_data
    
    def king_send_to_all(self, data: List[int]):
        """King向所有服务器发送数据"""
        if not self.is_king:
            return
        
        for i in range(1, Params.NUM_SERVER):
            queue_key = f"from_king_{i}"
            with self.lock:
                if queue_key not in self.message_queues:
                    self.message_queues[queue_key] = []
                self.message_queues[queue_key].append(data.copy())
