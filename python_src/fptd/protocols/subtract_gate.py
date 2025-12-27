"""
减法门
"""

from typing import List
from .gate import Gate
from ..params import Params
from ..share import Share


class SubtractGate(Gate):
    """减法门 - 向量减法 (本地计算，无通信)"""
    
    def __init__(self, input_gate1: Gate, input_gate2: Gate):
        """
        Args:
            input_gate1: 被减数门
            input_gate2: 减数门
        """
        if input_gate1.output_size != input_gate2.output_size:
            raise ValueError("Input gates must have the same output size")
        
        super().__init__(input_gate1.output_size)
        self.add_input_gate(input_gate1)
        self.add_input_gate(input_gate2)
    
    def run_online(self):
        """执行减法 (本地计算)"""
        lambdas1 = self.get_input_lambda_shares(0)
        lambdas2 = self.get_input_lambda_shares(1)
        deltas1 = self.get_input_delta_clears(0)
        deltas2 = self.get_input_delta_clears(1)
        
        # [λ_out] = [λ1] - [λ2]
        self.lambda_share_list = [l1 - l2 for l1, l2 in zip(lambdas1, lambdas2)]
        
        # Δ_out = Δ1 - Δ2
        self.delta_clear_list = [(d1 - d2) % Params.P for d1, d2 in zip(deltas1, deltas2)]
