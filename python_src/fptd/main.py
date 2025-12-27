"""
FPTD 主程序入口
"""

import os
import sys
import argparse
import time

# 添加项目根目录到路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from fptd.params import Params
from fptd.utils.data_manager import DataManager
from fptd.truth_discovery.td_online import run_truth_discovery


def main():
    """主函数"""
    parser = argparse.ArgumentParser(description='FPTD - Fast Privacy-Preserving Truth Discovery')
    parser.add_argument('--iterations', '-i', type=int, default=None,
                        help='Number of iterations')
    parser.add_argument('--quiet', '-q', action='store_true',
                        help='Quiet mode (less output)')
    args = parser.parse_args()
    
    if args.iterations:
        Params.ITER_TD = args.iterations
    if args.quiet:
        Params.IS_PRINT_EXE_INFO = False
    
    if Params.IS_PRINT_EXE_INFO:
        print("=" * 60)
        print("FPTD - Fast Privacy-Preserving Truth Discovery")
        print("=" * 60)
    
    # 获取项目根目录
    project_root = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
    
    # 数据文件路径
    sensing_data_file = os.path.join(project_root, Params.SENSING_DATA_FILE)
    truth_file = os.path.join(project_root, Params.TRUTH_FILE)
    
    if Params.IS_PRINT_EXE_INFO:
        print(f"\nLoading data from:")
        print(f"  Sensing data: {sensing_data_file}")
        print(f"  Truth data: {truth_file}")
    
    # 读取数据
    data_manager = DataManager(
        sensing_data_file, 
        truth_file, 
        is_categorical=Params.IS_CATEGORICAL_DATA
    )
    
    worker_num = data_manager.worker_num
    exam_num = data_manager.exam_num
    
    if Params.IS_PRINT_EXE_INFO:
        print(f"\nDataset statistics:")
        print(f"  Number of workers: {worker_num}")
        print(f"  Number of exams: {exam_num}")
        print(f"  Iterations: {Params.ITER_TD}")
    
    # 运行真值发现
    if Params.IS_PRINT_EXE_INFO:
        print("\n" + "-" * 60)
        print("Running Truth Discovery...")
        print("-" * 60)
    
    start_time = time.time()
    predictions = run_truth_discovery(data_manager)
    elapsed_time = time.time() - start_time
    
    # 评估结果
    if Params.IS_PRINT_EXE_INFO:
        print("\n" + "-" * 60)
        print("Evaluating results...")
        print("-" * 60)
    
    rmse, mae = data_manager.evaluate_predictions(predictions)
    
    print(f"\nResults:")
    print(f"  RMSE: {rmse:.4f}")
    print(f"  MAE: {mae:.4f}")
    print(f"  Time: {elapsed_time:.4f}s")
    
    # 显示部分预测结果
    if Params.IS_PRINT_EXE_INFO:
        print(f"\nSample predictions (first 10):")
        for i in range(min(10, len(predictions))):
            exam_id = data_manager.exam_ids[i]
            pred = predictions[i]
            truth = data_manager.truth_data.get(exam_id, "N/A")
            print(f"  Exam {exam_id}: Predicted={pred:.2f}, Truth={truth}")
    
    print("\n" + "=" * 60)
    print("FPTD completed successfully!")
    print("=" * 60)
    
    return predictions


if __name__ == "__main__":
    main()
