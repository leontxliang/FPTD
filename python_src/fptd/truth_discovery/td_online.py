"""
真值发现在线阶段
"""

from typing import List
from ..params import Params
from ..protocols.circuit import Circuit
from ..protocols.gate import Gate
from ..utils.data_manager import DataManager
from ..utils.tool import Tool


class TDOnline:
    """真值发现在线阶段"""
    
    def __init__(self, worker_num: int, exam_num: int):
        """
        Args:
            worker_num: 工人数量
            exam_num: 问题数量
        """
        self.worker_num = worker_num
        self.exam_num = exam_num
    
    def build_circuit(self, worker_data: List[List[int]], 
                      filter_matrix: List[List[bool]],
                      owner_idx: int, job_name: str) -> Circuit:
        """
        构建真值发现电路
        
        Args:
            worker_data: worker_data[worker][exam] 感知数据
            filter_matrix: filter_matrix[worker][exam] 是否有数据
            owner_idx: 电路所有者索引
            job_name: 任务名称
            
        Returns:
            构建好的电路
        """
        circuit = Circuit(owner_idx)
        circuit.set_job_name(job_name)
        
        # 创建输入门
        sensing_data_gates: List[Gate] = []
        for w in range(self.worker_num):
            gate = circuit.input(self.exam_num, worker_data[w])
            sensing_data_gates.append(gate)
        
        # 初始真值估计 (使用简单平均)
        initial_truth = self._compute_initial_truth(worker_data, filter_matrix)
        estimated_truth_gate = circuit.input(self.exam_num, initial_truth)
        
        # 迭代
        for iter_idx in range(Params.ITER_TD):
            # 计算距离
            sub_gates: List[Gate] = []
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
            weight_gates: List[Gate] = []
            for w in range(self.worker_num):
                # 计算 sum_all_distance / distance[w]^2
                div_gate = circuit.dot_prod_then_div(
                    sub_gates[w], sub_gates[w], out_sum_all_distance
                )
                # 计算 log
                log_gate = circuit.logarithm(div_gate)
                weight_gates.append(log_gate)
            
            # 输出权重
            for w in range(self.worker_num):
                circuit.output(weight_gates[w])
            
            # 计算新的真值估计
            new_truth_values = [0] * self.exam_num
            estimated_truth_gate = circuit.input(self.exam_num, new_truth_values)
        
        # 最终输出
        final_output = circuit.output(estimated_truth_gate)
        
        return circuit
    
    def _compute_initial_truth(self, worker_data: List[List[int]], 
                                filter_matrix: List[List[bool]]) -> List[int]:
        """计算初始真值估计 (简单平均)"""
        initial_truth = []
        
        for e in range(self.exam_num):
            total = 0
            count = 0
            for w in range(self.worker_num):
                if filter_matrix[w][e]:
                    total += worker_data[w][e]
                    count += 1
            
            if count > 0:
                avg = total // count
            else:
                avg = 0
            initial_truth.append(avg % Params.P)
        
        return initial_truth


def run_truth_discovery(data_manager: DataManager, job_name: str = "TD_optimal"):
    """
    运行完整的真值发现流程 (简化版，单机模拟)
    
    Args:
        data_manager: 数据管理器
        job_name: 任务名称
        
    Returns:
        预测结果列表
    """
    worker_num = data_manager.get_worker_num()
    exam_num = data_manager.get_exam_num()
    
    if Params.IS_PRINT_EXE_INFO:
        print(f"Running Truth Discovery with {worker_num} workers and {exam_num} exams")
    
    # 简化版：直接计算 (不使用MPC)
    worker_data = data_manager.sensing_data_matrix
    filter_matrix = data_manager.filter_matrix
    
    # 初始真值估计
    estimated_truth = []
    for e in range(exam_num):
        total = 0
        count = 0
        for w in range(worker_num):
            if filter_matrix[w][e]:
                total += Tool.to_float(worker_data[w][e])
                count += 1
        estimated_truth.append(total / count if count > 0 else 0)
    
    # 迭代
    for iter_idx in range(Params.ITER_TD):
        # 计算距离
        distances = []
        for w in range(worker_num):
            dist = 0
            for e in range(exam_num):
                if filter_matrix[w][e]:
                    diff = Tool.to_float(worker_data[w][e]) - estimated_truth[e]
                    dist += diff ** 2
            distances.append(dist)
        
        # 计算总距离
        sum_all_distance = sum(distances)
        
        # 计算权重
        weights = []
        for w in range(worker_num):
            if distances[w] > 0:
                weight = max(0, -1 * (distances[w] / sum_all_distance - 1))
                # 使用对数权重
                import math
                if weight > 0:
                    weight = math.log(sum_all_distance / distances[w])
                else:
                    weight = 0
            else:
                weight = 1
            weights.append(weight)
        
        # 更新真值估计
        new_truth = []
        for e in range(exam_num):
            weighted_sum = 0
            weight_sum = 0
            for w in range(worker_num):
                if filter_matrix[w][e]:
                    weighted_sum += Tool.to_float(worker_data[w][e]) * weights[w]
                    weight_sum += weights[w]
            
            if weight_sum > 0:
                new_truth.append(weighted_sum / weight_sum)
            else:
                new_truth.append(estimated_truth[e])
        
        estimated_truth = new_truth
        
        if Params.IS_PRINT_EXE_INFO:
            print(f"Iteration {iter_idx + 1}/{Params.ITER_TD} completed")
    
    return estimated_truth
