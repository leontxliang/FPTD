"""
逐元素乘法门
"""

from typing import List
from .gate import Gate
from ..params import Params
from ..share import Share


class ElemWiseMultiplyGate(Gate):
    """逐元素乘法门 - 使用Beaver三元组协议"""
    
    def __init__(self, input_gate1: Gate, input_gate2: Gate):
        """
        Args:
            input_gate1: 第一个输入向量门
            input_gate2: 第二个输入向量门
        """
        if input_gate1.output_size != input_gate2.output_size:
            raise ValueError("Input gates must have the same output size")
        
        super().__init__(input_gate1.output_size)
        self.add_input_gate(input_gate1)
        self.add_input_gate(input_gate2)
        
        # Beaver三元组份额
        self.a_shares: List[Share] = []
        self.b_shares: List[Share] = []
        self.c_shares: List[Share] = []  # c[i] = a[i] * b[i]
    
    def run_online(self):
        """执行逐元素乘法协议"""
        server = self.get_server()
        party_id = self.get_party_id()
        
        lambdas1 = self.get_input_lambda_shares(0)
        lambdas2 = self.get_input_lambda_shares(1)
        deltas1 = self.get_input_delta_clears(0)
        deltas2 = self.get_input_delta_clears(1)
        
        n = self.output_size
        
        # 计算 δx = Δx - λx + a, δy = Δy - λy + b
        delta_x = []
        delta_y = []
        for i in range(n):
            dx = (deltas1[i] - lambdas1[i].shr + self.a_shares[i].shr) % Params.P
            dy = (deltas2[i] - lambdas2[i].shr + self.b_shares[i].shr) % Params.P
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
        
        # 计算输出
        self.delta_clear_list = []
        for i in range(n):
            # [z[i]] = [c[i]] + δx[i]·[b[i]] + δy[i]·[a[i]] - δx[i]·δy[i] + [λz[i]]
            z_share = self.c_shares[i].shr
            z_share = (z_share + delta_x_clear[i] * self.b_shares[i].shr) % Params.P
            z_share = (z_share + delta_y_clear[i] * self.a_shares[i].shr) % Params.P
            
            if party_id == 0:
                z_share = (z_share - delta_x_clear[i] * delta_y_clear[i]) % Params.P
            
            z_share = (z_share + self.lambda_share_list[i].shr) % Params.P
            self.delta_clear_list.append(z_share)
        
        # 公开得到 Δz
        if server.is_king:
            all_z = server.king_read_from_all(n)
            self.delta_clear_list = [sum(all_z[p][i] for p in range(len(all_z))) % Params.P for i in range(n)]
            server.king_send_to_all(self.delta_clear_list)
        else:
            server.send_to_king(self.delta_clear_list)
            self.delta_clear_list = server.read_from_king(n)
