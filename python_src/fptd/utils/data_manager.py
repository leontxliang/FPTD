"""
数据管理类 (NumPy 版本)
"""

import csv
import numpy as np
from typing import Dict, Tuple, List
from ..params import Params
from .tool import Tool


class DataManager:
    """管理感知数据和真实值数据"""
    
    def __init__(self, sensing_data_file: str, truth_file: str, 
                 is_categorical: bool = False, required_worker_num: int = -1):
        """
        Args:
            sensing_data_file: 感知数据文件路径
            truth_file: 真实值文件路径
            is_categorical: 是否为分类数据
            required_worker_num: 需要的工人数量 (-1表示全部)
        """
        self.sensing_data_file = sensing_data_file
        self.truth_file = truth_file
        self.is_categorical = is_categorical
        self.required_worker_num = required_worker_num
        
        # 读取数据
        self.raw_sensing_data = self._read_sensing_data()
        self.truth_data = self._read_truth_data()
        
        # 转换为矩阵格式
        self.sensing_data_matrix, self.filter_matrix, self.exam_ids = self._change_to_matrix()
        
        # 真实值数组
        self.truth_array = np.array([self.truth_data.get(eid, np.nan) for eid in self.exam_ids])
    
    def _read_sensing_data(self) -> Dict[int, Dict[int, float]]:
        """读取感知数据"""
        worker2exam2answer: Dict[int, Dict[int, float]] = {}
        
        with open(self.sensing_data_file, 'r') as f:
            reader = csv.DictReader(f)
            for row in reader:
                exam_id = int(row['question'])
                worker_id = int(row['worker'])
                answer = float(row['answer'])
                
                if worker_id not in worker2exam2answer:
                    worker2exam2answer[worker_id] = {}
                worker2exam2answer[worker_id][exam_id] = answer
        
        return worker2exam2answer
    
    def _read_truth_data(self) -> Dict[int, float]:
        """读取真实值数据"""
        exam2truth: Dict[int, float] = {}
        
        with open(self.truth_file, 'r') as f:
            reader = csv.DictReader(f)
            for row in reader:
                exam_id = int(row['question'])
                truth = float(row['truth'])
                exam2truth[exam_id] = truth
        
        return exam2truth
    
    def _change_to_matrix(self) -> Tuple[np.ndarray, np.ndarray, List[int]]:
        """
        将感知数据转换为矩阵格式
        
        Returns:
            sensing_data_matrix: shape (worker_num, exam_num), 定点数格式
            filter_matrix: shape (worker_num, exam_num), 布尔值
            exam_ids: 问题ID列表
        """
        # 获取所有工人和问题
        worker_ids = sorted(self.raw_sensing_data.keys())
        if self.required_worker_num > 0:
            worker_ids = worker_ids[:self.required_worker_num]
        
        # 获取所有问题ID
        all_exam_ids = set()
        for worker_id in worker_ids:
            all_exam_ids.update(self.raw_sensing_data[worker_id].keys())
        exam_ids = sorted(all_exam_ids)
        
        worker_num = len(worker_ids)
        exam_num = len(exam_ids)
        
        # 构建矩阵
        sensing_data_matrix = np.zeros((worker_num, exam_num), dtype=object)
        filter_matrix = np.zeros((worker_num, exam_num), dtype=bool)
        
        for w_idx, worker_id in enumerate(worker_ids):
            worker_data = self.raw_sensing_data[worker_id]
            for e_idx, exam_id in enumerate(exam_ids):
                if exam_id in worker_data:
                    sensing_data_matrix[w_idx, e_idx] = Tool.to_fixed_point(worker_data[exam_id])
                    filter_matrix[w_idx, e_idx] = True
        
        return sensing_data_matrix, filter_matrix, exam_ids
    
    @property
    def worker_num(self) -> int:
        """工人数量"""
        return self.sensing_data_matrix.shape[0]
    
    @property
    def exam_num(self) -> int:
        """问题数量"""
        return self.sensing_data_matrix.shape[1]
    
    def get_worker_num(self) -> int:
        """获取工人数量"""
        return self.worker_num
    
    def get_exam_num(self) -> int:
        """获取问题数量"""
        return self.exam_num
    
    def get_sensing_data_for_exam(self, exam_idx: int) -> Tuple[np.ndarray, np.ndarray]:
        """获取某个问题的所有工人答案"""
        return self.sensing_data_matrix[:, exam_idx], self.filter_matrix[:, exam_idx]
    
    def get_sensing_data_for_worker(self, worker_idx: int) -> Tuple[np.ndarray, np.ndarray]:
        """获取某个工人的所有答案"""
        return self.sensing_data_matrix[worker_idx, :], self.filter_matrix[worker_idx, :]
    
    def get_float_data(self) -> np.ndarray:
        """获取浮点数格式的感知数据"""
        float_data = np.zeros((self.worker_num, self.exam_num), dtype=float)
        for w in range(self.worker_num):
            for e in range(self.exam_num):
                if self.filter_matrix[w, e]:
                    float_data[w, e] = Tool.to_float(int(self.sensing_data_matrix[w, e]))
        return float_data
    
    def evaluate_predictions(self, predictions: np.ndarray) -> Tuple[float, float]:
        """评估预测结果"""
        predictions = np.asarray(predictions)
        
        # 只评估有真实值的问题
        valid_mask = ~np.isnan(self.truth_array)
        valid_preds = predictions[valid_mask]
        valid_truths = self.truth_array[valid_mask]
        
        rmse = Tool.get_accuracy_rmse(valid_preds, valid_truths)
        mae = Tool.get_accuracy_mae(valid_preds, valid_truths)
        
        return rmse, mae
