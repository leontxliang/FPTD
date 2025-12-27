"""
门电路基类
"""

from abc import ABC, abstractmethod
from typing import List, Optional, TYPE_CHECKING

if TYPE_CHECKING:
    from .circuit import Circuit
    from ..edge_server import EdgeServer

from ..params import Params
from ..share import Share


class Gate(ABC):
    """门电路抽象基类"""
    
    def __init__(self, output_size: int):
        """
        Args:
            output_size: 输出向量大小
        """
        self.output_size = output_size
        
        # 输入门列表
        self.input_gates: List['Gate'] = []
        
        # 随机数份额 [λ]
        self.lambda_share_list: List[Share] = []
        
        # 明文 Δ = x + λ
        self.delta_clear_list: List[int] = []
        
        # 所属电路
        self.circuit: Optional['Circuit'] = None
        
        # 门ID
        self.gate_id: int = -1
    
    def set_circuit(self, circuit: 'Circuit', gate_id: int):
        """设置所属电路"""
        self.circuit = circuit
        self.gate_id = gate_id
    
    def get_server(self) -> 'EdgeServer':
        """获取边缘服务器"""
        return self.circuit.server
    
    def get_party_id(self) -> int:
        """获取参与方ID"""
        return self.circuit.owner_idx
    
    @abstractmethod
    def run_online(self):
        """执行在线计算"""
        pass
    
    def get_output_lambda_shares(self) -> List[Share]:
        """获取输出的λ份额"""
        return self.lambda_share_list
    
    def get_output_delta_clears(self) -> List[int]:
        """获取输出的Δ明文"""
        return self.delta_clear_list
    
    def add_input_gate(self, gate: 'Gate'):
        """添加输入门"""
        self.input_gates.append(gate)
    
    def get_input_gate(self, index: int = 0) -> 'Gate':
        """获取输入门"""
        return self.input_gates[index]
    
    def get_input_lambda_shares(self, index: int = 0) -> List[Share]:
        """获取输入门的λ份额"""
        return self.input_gates[index].get_output_lambda_shares()
    
    def get_input_delta_clears(self, index: int = 0) -> List[int]:
        """获取输入门的Δ明文"""
        return self.input_gates[index].get_output_delta_clears()
    
    def get_input_values(self, index: int = 0) -> List[int]:
        """获取输入值 (Δ - λ)"""
        lambdas = self.get_input_lambda_shares(index)
        deltas = self.get_input_delta_clears(index)
        return [(d - l.shr) % Params.P for d, l in zip(deltas, lambdas)]
