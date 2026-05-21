<template>
  <div class="facility-view">
    <h2>☕ 校园设施查询</h2>
    
    <el-card>
      <template #header>
        <div class="card-header">
          <span>🔍 搜索设施</span>
          <el-tag size="small" type="info">{{ facilities.length }} 个设施</el-tag>
        </div>
      </template>
      
      <el-input
        v-model="searchKeyword"
        placeholder="请输入关键词，例如：食堂、咖啡厅、图书馆..."
        clearable
        @keyup.enter="handleSearch"
        style="margin-bottom: 20px;"
        size="large"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </template>
      </el-input>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部设施" name="all">
          <template #label>
            <span>📋 全部设施</span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="食堂">
          <template #label>
            <span>🍜 食堂</span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="咖啡厅">
          <template #label>
            <span>☕ 咖啡厅</span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="阅览室">
          <template #label>
            <span>📚 图书馆</span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="自习室">
          <template #label>
            <span>✏️ 自习室</span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="实验室">
          <template #label>
            <span>🔬 实验室</span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <el-empty v-if="facilities.length === 0 && !loading" description="暂无设施数据" :image-size="120" />
      
      <el-table v-else :data="facilities" style="width: 100%" v-loading="loading" stripe>
        <el-table-column prop="name" label="设施名称" min-width="150" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag size="small" type="success">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="building.name" label="所属建筑" min-width="120">
          <template #default="scope">
            {{ scope.row.building?.name || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="locationDesc" label="位置" min-width="120" />
        <el-table-column prop="openTime" label="开放时间" width="150">
          <template #default="scope">
            <span v-if="scope.row.openTime">{{ scope.row.openTime }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="容量" width="80" align="center">
          <template #default="scope">
            {{ scope.row.capacity || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="设施详情" width="50%">
      <div v-if="currentFacility" class="facility-detail">
        <p><strong>📍 名称：</strong>{{ currentFacility.name }}</p>
        <p><strong>🏷️ 类型：</strong>{{ currentFacility.type }}</p>
        <p><strong>🏢 所属建筑：</strong>{{ currentFacility.building?.name || '未指定' }}</p>
        <p><strong>📌 位置：</strong>{{ currentFacility.locationDesc || '未指定' }}</p>
        <p><strong>🕐 开放时间：</strong>{{ currentFacility.openTime || '未指定' }}</p>
        <p><strong>👥 容量：</strong>{{ currentFacility.capacity ? currentFacility.capacity + '人' : '未指定' }}</p>
        <p><strong>📞 联系方式：</strong>{{ currentFacility.contact || '无' }}</p>
        <p><strong>📝 描述：</strong>{{ currentFacility.description || '无' }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { Search, View } from '@element-plus/icons-vue'
import { facilityAPI } from '../api'
import { ElMessage } from 'element-plus'

export default {
  name: 'FacilityView',
  components: {
    Search,
    View
  },
  setup() {
    const facilities = ref([])
    const searchKeyword = ref('')
    const activeTab = ref('all')
    const loading = ref(false)
    const dialogVisible = ref(false)
    const currentFacility = ref(null)

    const loadFacilities = async (type = null) => {
      loading.value = true
      try {
        console.log('加载设施数据，类型:', type)
        if (type && type !== 'all') {
          const data = await facilityAPI.getFacilitiesByType(type)
          console.log('设施数据:', data)
          facilities.value = data
        } else {
          // 获取所有设施 - 这里简化处理，实际应该有个getAll接口
          facilities.value = []
          // 分别获取各类型设施
          const types = ['食堂', '咖啡厅', '阅览室', '自习室', '实验室']
          for (const t of types) {
            const result = await facilityAPI.getFacilitiesByType(t)
            facilities.value.push(...result)
          }
          console.log('所有设施数据:', facilities.value)
        }
        
        if (facilities.value.length === 0) {
          ElMessage.info('暂无设施数据')
        }
      } catch (error) {
        console.error('加载设施失败:', error)
        ElMessage.error('加载设施失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    const handleSearch = async () => {
      if (!searchKeyword.value.trim()) {
        loadFacilities(activeTab.value === 'all' ? null : activeTab.value)
        return
      }
      
      loading.value = true
      try {
        console.log('搜索设施:', searchKeyword.value)
        facilities.value = await facilityAPI.searchFacilities(searchKeyword.value)
        console.log('搜索结果:', facilities.value)
        
        if (facilities.value.length === 0) {
          ElMessage.info('未找到匹配的设施')
        }
      } catch (error) {
        console.error('搜索失败:', error)
        ElMessage.error('搜索失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    const handleTabChange = (tab) => {
      searchKeyword.value = ''
      loadFacilities(tab === 'all' ? null : tab)
    }

    const viewDetail = (facility) => {
      currentFacility.value = facility
      dialogVisible.value = true
    }

    onMounted(() => {
      loadFacilities()
    })

    return {
      facilities,
      searchKeyword,
      activeTab,
      loading,
      dialogVisible,
      currentFacility,
      handleSearch,
      handleTabChange,
      viewDetail
    }
  }
}
</script>

<style scoped>
.facility-view {
  max-width: 1200px;
  margin: 0 auto;
}

h2 {
  color: #409EFF;
  margin-bottom: 20px;
  font-size: 26px;
  font-weight: bold;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

/* 表格样式优化 */
:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #303133;
  font-weight: bold;
  font-size: 14px;
}

:deep(.el-table td) {
  color: #606266;
  font-size: 14px;
}

/* 对话框样式 */
.facility-detail p {
  margin: 12px 0;
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
}

.facility-detail strong {
  color: #303133;
  font-weight: 600;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

:deep(.el-tabs__item) {
  font-size: 14px;
}
</style>
