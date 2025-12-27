"""
数据管理类
"""

import csv
import os
from typing import List, Dict, Tuple, Optional
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
        
        # 真实值列表 (按exam_id排序)
        self.truth_list = [self.truth_data.get(eid, 0.0) for eid in self.exam_ids]
    
    def _read_sensing_data(self) -> Dict[int, Dict[int, float]]:
        """
        读取感知数据
        
        Returns:
            worker2exam2answer[worker_id][exam_id] = answer
        """
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
        """
        读取真实值数据
        
        Returns:
            exam2truth[exam_id] = truth_value
        """
        exam2truth: Dict[int, float] = {}
        
        with open(self.truth_file, 'r') as f:
            reader = csv.DictReader(f)
            for row in reader:
                exam_id = int(row['question'])
                truth = float(row['truth'])
                exam2truth[exam_id] = truth
        
        return exam2truth
    
    def _change_to_matrix(self) -> Tuple[List[List[int]], List[List[bool]], List[int]]:
        """
        将感知数据转换为矩阵格式
        
        Returns:
            sensing_data_matrix[worker_idx][exam_idx]: 定点数格式的感知数据
            filter_matrix[worker_idx][exam_idx]: 是否有数据
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
        
        # 构建矩阵
        sensing_data_matrix: List[List[int]] = []
        filter_matrix: List[List[bool]] = []
        
        for worker_id in worker_ids:
            worker_data = self.raw_sensing_data[worker_id]
            row_data: List[int] = []
            row_filter: List[bool] = []
            
            for exam_id in exam_ids:
                if exam_id in worker_data:
                    # 转换为定点数
                    value = Tool.to_fixed_point(worker_data[exam_id])
                    row_data.append(value)
                    row_filter.append(True)
                else:
                    # 缺失数据用0填充
                    row_data.append(0)
                    row_filter.append(False)
            
            sensing_data_matrix.append(row_data)
            filter_matrix.append(row_filter)
        
        return sensing_data_matrix, filter_matrix, exam_ids
    
    def get_worker_num(self) -> int:
        """获取工人数量"""
        return len(self.sensing_data_matrix)
    
    def get_exam_num(self) -> int:
        """获取问题数量"""
        return len(self.exam_ids)
    
    def get_sensing_data_for_exam(self, exam_idx: int) -> Tuple[List[int], List[bool]]:
        """
        获取某个问题的所有工人答案
        
        Returns:
            data: 各工人的答案
            filter: 各工人是否有答案
        """
        data = [self.sensing_data_matrix[w][exam_idx] for w in range(self.get_worker_num())]
        filter_vec = [self.filter_matrix[w][exam_idx] for w in range(self.get_worker_num())]
        return data, filter_vec
    
    def get_sensing_data_for_worker(self, worker_idx: int) -> Tuple[List[int], List[bool]]:
        """
        获取某个工人的所有答案
        
        Returns:
            data: 各问题的答案
            filter: 各问题是否有答案
        """
        return self.sensing_data_matrix[worker_idx], self.filter_matrix[worker_idx]
    
    def evaluate_predictions(self, predictions: List[float]) -> Tuple[float, float]:
        """
        评估预测结果
        
        Args:
            predictions: 预测值列表
            
        Returns:
            (RMSE, MAE)
        """
        # 只评估有真实值的问题
        valid_preds = []
        valid_truths = []
        
        for i, exam_id in enumerate(self.exam_ids):
            if exam_id in self.truth_data:
                valid_preds.append(predictions[i])
                valid_truths.append(self.truth_data[exam_id])
        
        rmse = Tool.get_accuracy_rmse(valid_preds, valid_truths)
        mae = Tool.get_accuracy_mae(valid_preds, valid_truths)
        
        return rmse, mae
