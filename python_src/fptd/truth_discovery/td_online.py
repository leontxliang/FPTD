"""
真值发现在线阶段 (NumPy 向量化版本)
"""

import numpy as np
from ..params import Params
from ..utils.data_manager import DataManager


def run_truth_discovery(data_manager: DataManager) -> np.ndarray:
    """
    运行真值发现 (向量化版本)
    
    使用 CRH (Conflict Resolution on Heterogeneous Data) 算法
    """
    worker_num = data_manager.worker_num
    exam_num = data_manager.exam_num
    
    if Params.IS_PRINT_EXE_INFO:
        print(f"Running Vectorized Truth Discovery with {worker_num} workers and {exam_num} exams")
    
    # 获取数据
    worker_data = data_manager.get_float_data()
    filter_matrix = data_manager.filter_matrix.astype(float)
    
    # 初始真值估计
    count_per_exam = np.sum(filter_matrix, axis=0)
    count_per_exam = np.maximum(count_per_exam, 1)  # 避免除零
    estimated_truth = np.sum(worker_data * filter_matrix, axis=0) / count_per_exam
    
    # 迭代
    for iter_idx in range(Params.ITER_TD):
        # 计算距离
        diff = (worker_data - estimated_truth) * filter_matrix
        distances = np.sum(diff ** 2, axis=1)
        sum_all_distance = np.sum(distances)
        
        # 计算权重
        with np.errstate(divide='ignore', invalid='ignore'):
            weights = np.log(sum_all_distance / distances)
            weights = np.nan_to_num(weights, nan=0.0, posinf=0.0, neginf=0.0)
        weights = np.maximum(weights, 0)
        
        # 更新真值估计 (向量化)
        # weight_matrix[w, e] = weights[w] if filter_matrix[w, e] else 0
        weight_matrix = weights[:, np.newaxis] * filter_matrix
        weight_sum_per_exam = np.sum(weight_matrix, axis=0)
        weight_sum_per_exam = np.maximum(weight_sum_per_exam, 1e-10)
        
        weighted_data = worker_data * weight_matrix
        estimated_truth = np.sum(weighted_data, axis=0) / weight_sum_per_exam
        
        if Params.IS_PRINT_EXE_INFO:
            print(f"Iteration {iter_idx + 1}/{Params.ITER_TD} completed")
    
    return estimated_truth
