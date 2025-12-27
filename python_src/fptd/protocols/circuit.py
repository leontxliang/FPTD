"""
电路类
"""

from typing import List, Optional, TYPE_CHECKING
import os

if TYPE_CHECKING:
    from ..edge_server import EdgeServer

from .gate import Gate
from .input_gate import InputGate
from .output_gate import OutputGate
from .add_gate import AddGate
from .subtract_gate import SubtractGate
from .scaling_gate import ScalingGate
from .add_constant_gate import AddConstantGate
from .dot_product_gate import DotProductGate
from .elem_wise_multiply_gate import ElemWiseMultiplyGate
from .division_gate import DivisionGate
from .dot_prod_then_div_gate import DotProdThenDivGate
from .dot_prod_with_filter_gate import DotProdWithFilterGate
from .logarithm_gate import LogarithmGate
from .combination_gate import CombinationGate
from .reduce_gate import ReduceGate

from ..params import Params
from ..share import Share


class Circuit:
    """安全计算电路"""
    
    def __init__(self, owner_idx: int):
        """
        Args:
            owner_idx: 电路所有者索引 (参与方ID)
        """
        self.owner_idx = owner_idx
        self.gates: List[Gate] = []
        self.server: Optional['EdgeServer'] = None
        self.job_name: str = ""
    
    def set_server(self, server: 'EdgeServer'):
        """设置边缘服务器"""
        self.server = server
    
    def set_job_name(self, job_name: str):
        """设置任务名称"""
        self.job_name = job_name
    
    def add_gate(self, gate: Gate) -> Gate:
        """添加门到电路"""
        gate.set_circuit(self, len(self.gates))
        self.gates.append(gate)
        return gate
    
    # ==================== 门构建方法 ====================
    
    def input(self, size: int, values: List[int] = None) -> InputGate:
        """创建输入门"""
        gate = InputGate(size, values)
        return self.add_gate(gate)
    
    def output(self, input_gate: Gate) -> OutputGate:
        """创建输出门"""
        gate = OutputGate(input_gate)
        return self.add_gate(gate)
    
    def add(self, gate1: Gate, gate2: Gate) -> AddGate:
        """创建加法门"""
        gate = AddGate(gate1, gate2)
        return self.add_gate(gate)
    
    def subtract(self, gate1: Gate, gate2: Gate) -> SubtractGate:
        """创建减法门"""
        gate = SubtractGate(gate1, gate2)
        return self.add_gate(gate)
    
    def scale(self, gate: Gate, scalar: int) -> ScalingGate:
        """创建缩放门"""
        g = ScalingGate(gate, scalar)
        return self.add_gate(g)
    
    def add_constant(self, gate: Gate, constant: int) -> AddConstantGate:
        """创建常数加法门"""
        g = AddConstantGate(gate, constant)
        return self.add_gate(g)
    
    def dot_product(self, gate1: Gate, gate2: Gate) -> DotProductGate:
        """创建点积门"""
        gate = DotProductGate(gate1, gate2)
        return self.add_gate(gate)
    
    def elem_wise_multiply(self, gate1: Gate, gate2: Gate) -> ElemWiseMultiplyGate:
        """创建逐元素乘法门"""
        gate = ElemWiseMultiplyGate(gate1, gate2)
        return self.add_gate(gate)
    
    def division(self, gate: Gate, divisor: int) -> DivisionGate:
        """创建除法门"""
        g = DivisionGate(gate, divisor)
        return self.add_gate(g)
    
    def dot_prod_then_div(self, gate1: Gate, gate2: Gate, divisor_gate: Gate) -> DotProdThenDivGate:
        """创建点积后除法门"""
        gate = DotProdThenDivGate(gate1, gate2, divisor_gate)
        return self.add_gate(gate)
    
    def dot_prod_with_filter(self, gate1: Gate, gate2: Gate, filter_vec: List[bool]) -> DotProdWithFilterGate:
        """创建带过滤器的点积门"""
        gate = DotProdWithFilterGate(gate1, gate2, filter_vec)
        return self.add_gate(gate)
    
    def logarithm(self, gate: Gate) -> LogarithmGate:
        """创建对数门"""
        g = LogarithmGate(gate)
        return self.add_gate(g)
    
    def combination(self, gates: List[Gate]) -> CombinationGate:
        """创建组合门"""
        gate = CombinationGate(gates)
        return self.add_gate(gate)
    
    def reduce(self, gate: Gate) -> ReduceGate:
        """创建归约门"""
        g = ReduceGate(gate)
        return self.add_gate(g)
    
    # ==================== 执行方法 ====================
    
    def read_offline_from_file(self):
        """从文件读取离线数据"""
        offline_file = os.path.join(
            Params.OFFLINE_DATA_DIR, 
            f"{self.job_name}-party-{self.owner_idx}.txt"
        )
        
        if not os.path.exists(offline_file):
            if Params.IS_PRINT_EXE_INFO:
                print(f"Warning: Offline file not found: {offline_file}")
            return
        
        with open(offline_file, 'r') as f:
            lines = f.readlines()
        
        line_idx = 0
        for gate in self.gates:
            # 读取 lambda 份额
            num_lambdas = gate.output_size
            gate.lambda_share_list = []
            for _ in range(num_lambdas):
                if line_idx < len(lines):
                    val = int(lines[line_idx].strip())
                    gate.lambda_share_list.append(Share(self.owner_idx, val))
                    line_idx += 1
            
            # 读取特定门的额外数据
            if isinstance(gate, DotProductGate):
                n = gate.input_gates[0].output_size
                gate.a_shares = []
                gate.b_shares = []
                for _ in range(n):
                    if line_idx < len(lines):
                        gate.a_shares.append(Share(self.owner_idx, int(lines[line_idx].strip())))
                        line_idx += 1
                for _ in range(n):
                    if line_idx < len(lines):
                        gate.b_shares.append(Share(self.owner_idx, int(lines[line_idx].strip())))
                        line_idx += 1
                if line_idx < len(lines):
                    gate.c_share = Share(self.owner_idx, int(lines[line_idx].strip()))
                    line_idx += 1
            
            elif isinstance(gate, ElemWiseMultiplyGate):
                n = gate.output_size
                gate.a_shares = []
                gate.b_shares = []
                gate.c_shares = []
                for _ in range(n):
                    if line_idx < len(lines):
                        gate.a_shares.append(Share(self.owner_idx, int(lines[line_idx].strip())))
                        line_idx += 1
                for _ in range(n):
                    if line_idx < len(lines):
                        gate.b_shares.append(Share(self.owner_idx, int(lines[line_idx].strip())))
                        line_idx += 1
                for _ in range(n):
                    if line_idx < len(lines):
                        gate.c_shares.append(Share(self.owner_idx, int(lines[line_idx].strip())))
                        line_idx += 1
            
            elif isinstance(gate, DivisionGate):
                n = gate.output_size
                gate.r_shares = []
                gate.r1_shares = []
                gate.r2_shares = []
                for _ in range(n):
                    if line_idx < len(lines):
                        gate.r_shares.append(Share(self.owner_idx, int(lines[line_idx].strip())))
                        line_idx += 1
                for _ in range(n):
                    if line_idx < len(lines):
                        gate.r1_shares.append(Share(self.owner_idx, int(lines[line_idx].strip())))
                        line_idx += 1
                for _ in range(n):
                    if line_idx < len(lines):
                        gate.r2_shares.append(Share(self.owner_idx, int(lines[line_idx].strip())))
                        line_idx += 1
            
            elif isinstance(gate, DotProdThenDivGate):
                n = gate.input_gates[0].output_size
                gate.a_shares = []
                gate.b_shares = []
                for _ in range(n):
                    if line_idx < len(lines):
                        gate.a_shares.append(Share(self.owner_idx, int(lines[line_idx].strip())))
                        line_idx += 1
                for _ in range(n):
                    if line_idx < len(lines):
                        gate.b_shares.append(Share(self.owner_idx, int(lines[line_idx].strip())))
                        line_idx += 1
                if line_idx < len(lines):
                    gate.c_share = Share(self.owner_idx, int(lines[line_idx].strip()))
                    line_idx += 1
                if line_idx < len(lines):
                    gate.r_share = Share(self.owner_idx, int(lines[line_idx].strip()))
                    line_idx += 1
                if line_idx < len(lines):
                    gate.r1_share = Share(self.owner_idx, int(lines[line_idx].strip()))
                    line_idx += 1
                if line_idx < len(lines):
                    gate.r2_share = Share(self.owner_idx, int(lines[line_idx].strip()))
                    line_idx += 1
            
            elif isinstance(gate, DotProdWithFilterGate):
                n = gate.valid_count
                gate.a_shares = []
                gate.b_shares = []
                for _ in range(n):
                    if line_idx < len(lines):
                        gate.a_shares.append(Share(self.owner_idx, int(lines[line_idx].strip())))
                        line_idx += 1
                for _ in range(n):
                    if line_idx < len(lines):
                        gate.b_shares.append(Share(self.owner_idx, int(lines[line_idx].strip())))
                        line_idx += 1
                if line_idx < len(lines):
                    gate.c_share = Share(self.owner_idx, int(lines[line_idx].strip()))
                    line_idx += 1
    
    def run_online(self):
        """执行在线计算"""
        for gate in self.gates:
            gate.run_online()
    
    def get_output_gates(self) -> List[OutputGate]:
        """获取所有输出门"""
        return [g for g in self.gates if isinstance(g, OutputGate)]
