"""
点积后除法门 (优化版)
"""

from typing import List
from .gate import Gate
from ..params import Params
from ..share import Share


class DotProdThenDivGate(Gate):
    """点积后除法门 - 融合点积和除法操作"""
    
    L = Params.DIV_L
    E = Params.DIV_E
    SIGMA = Params.DIV_SIGMA
    
    def __init__(self, input_gate1: Gate, input_gate2: Gate, divisor_gate: Gate):
        """
        Args:
            input_gate1: 第一个输入向量门
            input_gate2: 第二个输入向量门
            divisor_gate: 除数门 (输出大小为1)
        """
        if input_gate1.output_size != input_gate2.output_size:
            raise ValueError("Input gates must have the same output size")
        
        super().__init__(1)  # 输出是标量
        self.add_input_gate(input_gate1)
        self.add_input_gate(input_gate2)
        self.add_input_gate(divisor_gate)
        
        # Beaver三元组
        self.a_shares: List[Share] = []
        self.b_shares: List[Share] = []
        self.c_share: Share = None
        
        # 除法随机数
        self.r_share: Share = None
        self.r1_share: Share = None
        self.r2_share: Share = None
    
    def run_online(self):
        """执行点积后除法协议"""
        server = self.get_server()
        party_id = self.get_party_id()
        
        lambdas1 = self.get_input_lambda_shares(0)
        lambdas2 = self.get_input_lambda_shares(1)
        deltas1 = self.get_input_delta_clears(0)
        deltas2 = self.get_input_delta_clears(1)
        
        # 获取除数
        divisor_deltas = self.get_input_delta_clears(2)
        divisor_lambdas = self.get_input_lambda_shares(2)
        divisor = (divisor_deltas[0] - divisor_lambdas[0].shr) % Params.P
        
        n = len(lambdas1)
        
        # 第一步: 点积
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
        
        # 计算点积份额
        prod_share = self.c_share.shr
        delta_xy_sum = 0
        
        for i in range(n):
            prod_share = (prod_share + delta_x_clear[i] * self.b_shares[i].shr) % Params.P
            prod_share = (prod_share + delta_y_clear[i] * self.a_shares[i].shr) % Params.P
            delta_xy_sum = (delta_xy_sum + delta_x_clear[i] * delta_y_clear[i]) % Params.P
        
        if party_id == 0:
            prod_share = (prod_share - delta_xy_sum) % Params.P
        
        # 第二步: 除法
        shift = 2 ** (self.L + self.SIGMA)
        
        # [h] = ([r] + 2^{l+σ}·[r1]) · d
        h_share = ((self.r_share.shr + shift * self.r1_share.shr) * divisor) % Params.P
        
        # [z] = 2^{l+σ}·[prod] + [h] + [r2]
        z_share = (shift * prod_share + h_share + self.r2_share.shr) % Params.P
        
        # 公开 z
        if server.is_king:
            all_z = server.king_read_from_all(1)
            z_clear = sum(all_z[p][0] for p in range(len(all_z))) % Params.P
            server.king_send_to_all([z_clear])
        else:
            server.send_to_king([z_share])
            z_clear = server.read_from_king(1)[0]
        
        # 计算输出
        divisor_shifted = shift * divisor if divisor != 0 else 1
        quotient = z_clear // divisor_shifted if divisor != 0 else 0
        
        out_share = (quotient - self.r1_share.shr + self.lambda_share_list[0].shr) % Params.P
        
        # 公开得到最终 Δ
        if server.is_king:
            all_out = server.king_read_from_all(1)
            self.delta_clear_list = [sum(all_out[p][0] for p in range(len(all_out))) % Params.P]
            server.king_send_to_all(self.delta_clear_list)
        else:
            server.send_to_king([out_share])
            self.delta_clear_list = server.read_from_king(1)
