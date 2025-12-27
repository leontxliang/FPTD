"""
除法门
"""

from typing import List
from .gate import Gate
from ..params import Params
from ..share import Share


class DivisionGate(Gate):
    """除法门 - 安全截断协议"""
    
    # 协议参数
    L = Params.DIV_L      # 除数位长
    E = Params.DIV_E      # 被除数位长
    SIGMA = Params.DIV_SIGMA  # 安全参数
    
    def __init__(self, input_gate: Gate, divisor: int):
        """
        Args:
            input_gate: 被除数门
            divisor: 除数 (明文)
        """
        super().__init__(input_gate.output_size)
        self.add_input_gate(input_gate)
        self.divisor = divisor
        
        # 预计算的随机数份额
        self.r_shares: List[Share] = []    # [r]
        self.r1_shares: List[Share] = []   # [r1]
        self.r2_shares: List[Share] = []   # [r2]
    
    def run_online(self):
        """
        执行除法协议
        
        输入: [x] (被除数份额), d (除数)
        1. 计算 [h] = ([r] + 2^{l+σ}·[r1]) · d
        2. 计算 [z] = 2^{l+σ}·[x] + [h] + [r2]
        3. 公开 z
        4. 计算 Δ = ⌊z / (2^{l+σ}·d)⌋
        5. 输出: Δ - [r1] = x/d
        """
        server = self.get_server()
        party_id = self.get_party_id()
        
        lambdas = self.get_input_lambda_shares()
        deltas = self.get_input_delta_clears()
        
        n = self.output_size
        shift = 2 ** (self.L + self.SIGMA)
        divisor_shifted = shift * self.divisor
        
        z_shares = []
        for i in range(n):
            # x = Δ - λ
            x_share = (deltas[i] - lambdas[i].shr) % Params.P
            
            # [h] = ([r] + 2^{l+σ}·[r1]) · d
            h_share = ((self.r_shares[i].shr + shift * self.r1_shares[i].shr) * self.divisor) % Params.P
            
            # [z] = 2^{l+σ}·[x] + [h] + [r2]
            z_share = (shift * x_share + h_share + self.r2_shares[i].shr) % Params.P
            z_shares.append(z_share)
        
        # 公开 z
        if server.is_king:
            all_z = server.king_read_from_all(n)
            z_clear = [sum(all_z[p][i] for p in range(len(all_z))) % Params.P for i in range(n)]
            server.king_send_to_all(z_clear)
        else:
            server.send_to_king(z_shares)
            z_clear = server.read_from_king(n)
        
        # 计算输出
        self.delta_clear_list = []
        for i in range(n):
            # Δ = ⌊z / (2^{l+σ}·d)⌋
            quotient = z_clear[i] // divisor_shifted
            
            # 输出 = Δ - [r1] + [λz]
            out_share = (quotient - self.r1_shares[i].shr + self.lambda_share_list[i].shr) % Params.P
            self.delta_clear_list.append(out_share)
        
        # 公开得到最终 Δ
        if server.is_king:
            all_out = server.king_read_from_all(n)
            self.delta_clear_list = [sum(all_out[p][i] for p in range(len(all_out))) % Params.P for i in range(n)]
            server.king_send_to_all(self.delta_clear_list)
        else:
            server.send_to_king(self.delta_clear_list)
            self.delta_clear_list = server.read_from_king(n)
