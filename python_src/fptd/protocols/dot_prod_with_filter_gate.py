"""
带过滤器的点积门
"""

from typing import List
from .gate import Gate
from ..params import Params
from ..share import Share


class DotProdWithFilterGate(Gate):
    """带过滤器的点积门 - 只对过滤器为True的元素计算点积"""
    
    def __init__(self, input_gate1: Gate, input_gate2: Gate, filter_vec: List[bool]):
        """
        Args:
            input_gate1: 第一个输入向量门
            input_gate2: 第二个输入向量门
            filter_vec: 过滤器向量
        """
        if input_gate1.output_size != input_gate2.output_size:
            raise ValueError("Input gates must have the same output size")
        if input_gate1.output_size != len(filter_vec):
            raise ValueError("Filter vector must have the same size as input")
        
        super().__init__(1)  # 输出是标量
        self.add_input_gate(input_gate1)
        self.add_input_gate(input_gate2)
        self.filter_vec = filter_vec
        
        # 有效元素数量
        self.valid_count = sum(filter_vec)
        
        # Beaver三元组 (只对有效元素)
        self.a_shares: List[Share] = []
        self.b_shares: List[Share] = []
        self.c_share: Share = None
    
    def run_online(self):
        """执行带过滤器的点积协议"""
        server = self.get_server()
        party_id = self.get_party_id()
        
        lambdas1 = self.get_input_lambda_shares(0)
        lambdas2 = self.get_input_lambda_shares(1)
        deltas1 = self.get_input_delta_clears(0)
        deltas2 = self.get_input_delta_clears(1)
        
        # 过滤有效元素
        valid_lambdas1 = [l for l, f in zip(lambdas1, self.filter_vec) if f]
        valid_lambdas2 = [l for l, f in zip(lambdas2, self.filter_vec) if f]
        valid_deltas1 = [d for d, f in zip(deltas1, self.filter_vec) if f]
        valid_deltas2 = [d for d, f in zip(deltas2, self.filter_vec) if f]
        
        n = self.valid_count
        
        if n == 0:
            self.delta_clear_list = [0]
            return
        
        # 计算 δx, δy
        delta_x = []
        delta_y = []
        for i in range(n):
            dx = (valid_deltas1[i] - valid_lambdas1[i].shr + self.a_shares[i].shr) % Params.P
            dy = (valid_deltas2[i] - valid_lambdas2[i].shr + self.b_shares[i].shr) % Params.P
            delta_x.append(dx)
            delta_y.append(dy)
        
        # 公开 δx, δy
        if server.is_king:
            all_delta_x = server.king_read_from_all(n)
            all_delta_y = server.king_read_from_all(n)
            delta_x_clear = [sum(all_delta_x[p][i] for p in range(len(all_delta_x))) % Params.P for i in range(n)]
            delta_y_clear = [sum(all_delta_y[p][i] for p in range(len(all_delta_y))) % Params.P for i in range(n)]
            server.king_send_to_all(delta_x_clear)
            server.king_send_to_all(delta_y_clear)
        else:
            server.send_to_king(delta_x)
            server.send_to_king(delta_y)
            delta_x_clear = server.read_from_king(n)
            delta_y_clear = server.read_from_king(n)
        
        # 计算点积份额
        z_share = self.c_share.shr
        delta_xy_sum = 0
        
        for i in range(n):
            z_share = (z_share + delta_x_clear[i] * self.b_shares[i].shr) % Params.P
            z_share = (z_share + delta_y_clear[i] * self.a_shares[i].shr) % Params.P
            delta_xy_sum = (delta_xy_sum + delta_x_clear[i] * delta_y_clear[i]) % Params.P
        
        if party_id == 0:
            z_share = (z_share - delta_xy_sum) % Params.P
        
        z_share = (z_share + self.lambda_share_list[0].shr) % Params.P
        
        # 公开 z
        if server.is_king:
            all_z = server.king_read_from_all(1)
            delta_z = sum(all_z[p][0] for p in range(len(all_z))) % Params.P
            server.king_send_to_all([delta_z])
            self.delta_clear_list = [delta_z]
        else:
            server.send_to_king([z_share])
            self.delta_clear_list = server.read_from_king(1)
