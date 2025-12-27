"""
离线数据生成器 (模拟可信第三方)
"""

import os
from typing import List
from ..params import Params
from ..share import Share
from ..sharing.shamir_sharing import ShamirSharing
from ..utils.tool import Tool


class FakeParty:
    """模拟可信第三方，为各参与方生成离线随机数和预计算数据"""
    
    def __init__(self, job_name: str):
        """
        Args:
            job_name: 任务名称
        """
        self.job_name = job_name
        self.sharing = ShamirSharing()
        
        # 各参与方的离线数据
        self.party_data: List[List[int]] = [[] for _ in range(Params.NUM_SERVER)]
    
    def generate_random_shares(self) -> List[Share]:
        """生成随机数的份额"""
        rand_val = Tool.get_rand()
        return self.sharing.get_shares(rand_val)
    
    def generate_zero_shares(self) -> List[Share]:
        """生成零的份额"""
        return self.sharing.get_shares(0)
    
    def generate_beaver_triple(self) -> tuple:
        """
        生成Beaver三元组 (a, b, c) 其中 c = a * b
        
        Returns:
            (a_shares, b_shares, c_shares)
        """
        a = Tool.get_rand()
        b = Tool.get_rand()
        c = (a * b) % Params.P
        
        a_shares = self.sharing.get_shares(a)
        b_shares = self.sharing.get_shares(b)
        c_shares = self.sharing.get_shares(c)
        
        return a_shares, b_shares, c_shares
    
    def generate_division_randoms(self, l: int, sigma: int) -> tuple:
        """
        生成除法协议所需的随机数
        
        Args:
            l: 除数位长
            sigma: 安全参数
            
        Returns:
            (r_shares, r1_shares, r2_shares)
        """
        r = Tool.get_rand_below(2 ** l)
        r1 = Tool.get_rand_below(2 ** (l + sigma))
        r2 = Tool.get_rand_below(2 ** sigma)
        
        r_shares = self.sharing.get_shares(r)
        r1_shares = self.sharing.get_shares(r1)
        r2_shares = self.sharing.get_shares(r2)
        
        return r_shares, r1_shares, r2_shares
    
    def add_shares_to_parties(self, shares: List[Share]):
        """将份额添加到各参与方的数据中"""
        for i, share in enumerate(shares):
            self.party_data[i].append(share.shr)
    
    def add_value_to_all_parties(self, value: int):
        """将明文值添加到所有参与方"""
        for i in range(Params.NUM_SERVER):
            self.party_data[i].append(value)
    
    def write_to_files(self):
        """将离线数据写入文件"""
        os.makedirs(Params.OFFLINE_DATA_DIR, exist_ok=True)
        
        for party_idx in range(Params.NUM_SERVER):
            file_path = os.path.join(
                Params.OFFLINE_DATA_DIR,
                f"{self.job_name}-party-{party_idx}.txt"
            )
            
            with open(file_path, 'w') as f:
                for value in self.party_data[party_idx]:
                    f.write(f"{value}\n")
        
        if Params.IS_PRINT_EXE_INFO:
            print(f"Offline data written to {Params.OFFLINE_DATA_DIR}/")
