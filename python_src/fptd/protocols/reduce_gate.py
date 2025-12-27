"""
归约门
"""

from typing import List
from .gate import Gate
from ..params import Params
from ..share import Share


class ReduceGate(Gate):
    """归约门 - 将向量归约为标量 (求和)"""
    
    def __init__(self, input_gate: Gate):
        """
        Args:
            input_gate: 输入门
        """
        super().__init__(1)
        self.add_input_gate(input_gate)
    
    def run_online(self):
        """执行归约 (本地计算)"""
        lambdas = self.get_input_lambda_shares()
        deltas = self.get_input_delta_clears()
        
        party_id = self.get_party_id()
        
        # 求和
        lambda_sum = sum(l.shr for l in lambdas) % Params.P
        delta_sum = sum(deltas) % Params.P
        
        self.lambda_share_list = [Share(party_id, lambda_sum)]
        self.delta_clear_list = [delta_sum]
