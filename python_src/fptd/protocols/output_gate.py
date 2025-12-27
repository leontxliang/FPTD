"""
输出门
"""

from typing import List
from .gate import Gate
from ..params import Params
from ..share import Share
from ..sharing.shamir_sharing import ShamirSharing


class OutputGate(Gate):
    """输出门 - 恢复明文结果"""
    
    def __init__(self, input_gate: Gate):
        """
        Args:
            input_gate: 输入门
        """
        super().__init__(input_gate.output_size)
        self.add_input_gate(input_gate)
        self.output_values: List[int] = []
    
    def run_online(self):
        """
        执行输出门协议
        
        协议流程:
        1. 各参与方发送 λ 份额给 King
        2. King 恢复 λ 并计算 x = Δ - λ
        3. King 广播结果
        """
        server = self.get_server()
        
        input_lambdas = self.get_input_lambda_shares()
        input_deltas = self.get_input_delta_clears()
        
        if server.is_king:
            # 收集所有参与方的 λ 份额
            all_lambda_shares = server.king_read_shares_from_all(self.output_size)
            
            # 恢复 λ 并计算输出
            sharing = ShamirSharing()
            self.output_values = []
            
            for i in range(self.output_size):
                shares = [all_lambda_shares[p][i] for p in range(len(all_lambda_shares))]
                lambda_val = sharing.recover(shares)
                output_val = (input_deltas[i] - lambda_val) % Params.P
                self.output_values.append(output_val)
            
            # 广播结果
            server.king_send_to_all(self.output_values)
        else:
            # 发送 λ 份额
            server.send_shares_to_king(input_lambdas)
            # 接收结果
            self.output_values = server.read_from_king(self.output_size)
        
        # 设置输出
        self.delta_clear_list = self.output_values
        self.lambda_share_list = [Share(self.get_party_id(), 0) for _ in range(self.output_size)]
    
    def get_output_values(self) -> List[int]:
        """获取输出值"""
        return self.output_values
