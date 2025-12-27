"""
真值发现离线阶段
"""

from typing import List
from ..params import Params
from ..offline.offline_circuit import OfflineCircuit
from ..offline.offline_gate import (
    OfflineGate, OfflineInputGate, OfflineOutputGate,
    OfflineSubtractGate, OfflineDotProdWithFilterGate,
    OfflineDotProdThenDivGate, OfflineLogGate, OfflineCombinationGate
)


class TDOffline:
    """真值发现离线阶段"""
    
    def __init__(self, worker_num: int, exam_num: int, job_name: str):
        """
        Args:
            worker_num: 工人数量
            exam_num: 问题数量
            job_name: 任务名称
        """
        self.worker_num = worker_num
        self.exam_num = exam_num
        self.job_name = job_name
    
    def run_offline(self, filter_matrix: List[List[bool]] = None):
        """
        执行离线计算
        
        Args:
            filter_matrix: 过滤矩阵 filter_matrix[worker][exam]
        """
        if filter_matrix is None:
            filter_matrix = [[True] * self.exam_num for _ in range(self.worker_num)]
        
        circuit = OfflineCircuit(self.job_name)
        
        # 创建输入门
        sensing_data_gates: List[OfflineInputGate] = []
        for w in range(self.worker_num):
            gate = circuit.input(self.exam_num)
            sensing_data_gates.append(gate)
        
        # 初始真值估计
        estimated_truth_gate = circuit.input(self.exam_num)
        
        # 迭代
        for iter_idx in range(Params.ITER_TD):
            # 计算距离 (sensing_data - estimated_truth)
            sub_gates: List[OfflineGate] = []
            for w in range(self.worker_num):
                sub_gate = circuit.subtract(sensing_data_gates[w], estimated_truth_gate)
                sub_gates.append(sub_gate)
            
            # 组合所有距离
            all_sub_gate = circuit.combination(sub_gates)
            
            # 计算所有距离的平方和
            all_filter = []
            for w in range(self.worker_num):
                all_filter.extend(filter_matrix[w])
            
            sum_all_distance_gate = circuit.dot_prod_with_filter(
                all_sub_gate, all_sub_gate, all_filter
            )
            
            # 输出总距离
            out_sum_all_distance = circuit.output(sum_all_distance_gate)
            
            # 计算每个工人的权重
            weight_gates: List[OfflineGate] = []
            for w in range(self.worker_num):
                # 计算 distance[w]^2 / sum_all_distance
                div_gate = circuit.dot_prod_then_div(
                    sub_gates[w], sub_gates[w], out_sum_all_distance
                )
                # 计算 log(1 / div_gate) = -log(div_gate)
                log_gate = circuit.logarithm(div_gate)
                weight_gates.append(log_gate)
            
            # 输出权重
            for w in range(self.worker_num):
                circuit.output(weight_gates[w])
            
            # 计算新的真值估计
            new_truth_gates: List[OfflineGate] = []
            for e in range(self.exam_num):
                # 获取该问题的过滤器
                exam_filter = [filter_matrix[w][e] for w in range(self.worker_num)]
                
                # 组合所有工人对该问题的数据
                exam_data_gates = []
                for w in range(self.worker_num):
                    # 这里简化处理，实际需要提取单个元素
                    exam_data_gates.append(weight_gates[w])
                
                # 计算权重和
                combined_weights = circuit.combination(weight_gates)
                sum_weights_gate = circuit.dot_prod_with_filter(
                    combined_weights, 
                    circuit.input(self.worker_num),  # 全1向量
                    exam_filter
                )
                out_sum_weights = circuit.output(sum_weights_gate)
                
                # 计算加权平均
                combined_data = circuit.combination(sensing_data_gates)
                new_truth_gate = circuit.dot_prod_then_div(
                    combined_data, combined_weights, out_sum_weights
                )
                new_truth_gates.append(new_truth_gate)
            
            # 组合新的真值估计
            estimated_truth_gate = circuit.combination(new_truth_gates)
        
        # 最终输出
        circuit.output(estimated_truth_gate)
        
        # 执行离线计算
        circuit.run_offline()
        
        if Params.IS_PRINT_EXE_INFO:
            print(f"Offline phase completed for {self.job_name}")
