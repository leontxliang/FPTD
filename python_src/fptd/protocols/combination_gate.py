"""
组合门
"""

from typing import List
from .gate import Gate
from ..params import Params
from ..share import Share


class CombinationGate(Gate):
    """组合门 - 将多个门的输出组合成一个向量"""
    
    def __init__(self, input_gates: List[Gate]):
        """
        Args:
            input_gates: 输入门列表
        """
        total_size = sum(g.output_size for g in input_gates)
        super().__init__(total_size)
        
        for gate in input_gates:
            self.add_input_gate(gate)
    
    def run_online(self):
        """执行组合 (本地计算)"""
        self.lambda_share_list = []
        self.delta_clear_list = []
        
        for gate in self.input_gates:
            self.lambda_share_list.extend(gate.get_output_lambda_shares())
            self.delta_clear_list.extend(gate.get_output_delta_clears())
