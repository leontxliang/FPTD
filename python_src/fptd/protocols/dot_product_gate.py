"""
点积门
"""

from typing import List
from .gate import Gate
from ..params import Params
from ..share import Share


class DotProductGate(Gate):
    """点积门 - 使用Beaver三元组协议"""
    
    def __init__(self, input_gate1: Gate, input_gate2: Gate):
        """
        Args:
            input_gate1: 第一个输入向量门
            input_gate2: 第二个输入向量门
        """
        if input_gate1.output_size != input_gate2.output_size:
            raise ValueError("Input gates must have the same output size")
        
        super().__init__(1)  # 点积输出是标量
        self.add_input_gate(input_gate1)
        self.add_input_gate(input_gate2)
        
        # Beaver三元组份额
        self.a_shares: List[Share] = []
        self.b_shares: List[Share] = []
        self.c_share: Share = None  # c = a · b
    
    def run_online(self):
        """
        执行点积协议 (Beaver三元组)
        
        预计算: [a], [b], [c] 其中 c = a · b
        输入: [x], [y]
        
        1. 计算 δx = Δx - λx + a
        2. 计算 δy = Δy - λy + b
        3. 公开 δx, δy
        4. 计算 [z] = [c] + δx·[b] + δy·[a] - δx·δy + [λz]
        5. 输出 Δz = z (公开)
        """
        server = self.get_server()
        party_id = self.get_party_id()
        
        lambdas1 = self.get_input_lambda_shares(0)
        lambdas2 = self.get_input_lambda_shares(1)
        deltas1 = self.get_input_delta_clears(0)
        deltas2 = self.get_input_delta_clears(1)
        
        n = len(lambdas1)
        
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
            # 求和得到公开值
            delta_x_clear = [sum(all_delta_x[p][i] for p in range(len(all_delta_x))) % Params.P for i in range(n)]
            delta_y_clear = [sum(all_delta_y[p][i] for p in range(len(all_delta_y))) % Params.P for i in range(n)]
            server.king_send_to_all(delta_x_clear)
            server.king_send_to_all(delta_y_clear)
        else:
            server.send_to_king(delta_x)
            server.send_to_king(delta_y)
            delta_x_clear = server.read_from_king(n)
            delta_y_clear = server.read_from_king(n)
        
        # 计算输出份额
        # [z] = [c] + Σ(δx[i]·[b[i]] + δy[i]·[a[i]]) - Σ(δx[i]·δy[i]) + [λz]
        z_share = self.c_share.shr
        delta_xy_sum = 0
        
        for i in range(n):
            z_share = (z_share + delta_x_clear[i] * self.b_shares[i].shr) % Params.P
            z_share = (z_share + delta_y_clear[i] * self.a_shares[i].shr) % Params.P
            delta_xy_sum = (delta_xy_sum + delta_x_clear[i] * delta_y_clear[i]) % Params.P
        
        # 只有 party 0 减去 δx·δy
        if party_id == 0:
            z_share = (z_share - delta_xy_sum) % Params.P
        
        z_share = (z_share + self.lambda_share_list[0].shr) % Params.P
        
        # 公开 z 得到 Δz
        if server.is_king:
            all_z = server.king_read_from_all(1)
            delta_z = sum(all_z[p][0] for p in range(len(all_z))) % Params.P
            server.king_send_to_all([delta_z])
            self.delta_clear_list = [delta_z]
        else:
            server.send_to_king([z_share])
            self.delta_clear_list = server.read_from_king(1)
