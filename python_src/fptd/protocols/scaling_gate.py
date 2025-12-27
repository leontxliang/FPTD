"""
缩放门
"""

from typing import List
from .gate import Gate
from ..params import Params
from ..share import Share


class ScalingGate(Gate):
    """缩放门 - 常数乘法 (本地计算，无通信)"""
    
    def __init__(self, input_gate: Gate, scalar: int):
        """
        Args:
            input_gate: 输入门
            scalar: 缩放常数
        """
        super().__init__(input_gate.output_size)
        self.add_input_gate(input_gate)
        self.scalar = scalar % Params.P
    
    def run_online(self):
        """执行缩放 (本地计算)"""
        lambdas = self.get_input_lambda_shares()
        deltas = self.get_input_delta_clears()
        
        # [λ_out] = c * [λ]
        self.lambda_share_list = [l * self.scalar for l in lambdas]
        
        # Δ_out = c * Δ
        self.delta_clear_list = [(d * self.scalar) % Params.P for d in deltas]
