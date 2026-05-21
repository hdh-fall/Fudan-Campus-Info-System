<template>
  <div class="campus-view">
    <h2>校区建筑查询</h2>
    
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>📍 校区列表</span>
              <el-tag size="small" type="info">{{ campuses.length }} 个校区</el-tag>
            </div>
          </template>
          <el-empty v-if="campuses.length === 0" description="暂无校区数据" :image-size="80" />
          <el-menu v-else @select="handleCampusSelect" default-active="">
            <el-menu-item 
              v-for="campus in campuses" 
              :key="campus.campusId" 
              :index="String(campus.campusId)"
            >
              <el-icon><Location /></el-icon>
              <span>{{ campus.name }}</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span v-if="selectedCampus">🏢 {{ selectedCampus.name }} - 建筑列表</span>
              <span v-else>🏢 建筑列表</span>
              <el-tag size="small" type="success">{{ buildings.length }} 栋建筑</el-tag>
            </div>
          </template>
          
          <el-empty v-if="!selectedCampus" description="请从左侧选择一个校区查看建筑" :image-size="120" />
          <el-empty v-else-if="buildings.length === 0" description="该校区暂无建筑数据" :image-size="120" />
          
          <el-table v-else :data="buildings" style="width: 100%" v-loading="loading" stripe>
            <el-table-column prop="name" label="建筑名称" min-width="150" />
            <el-table-column prop="type" label="类型" width="120">
              <template #default="scope">
                <el-tag size="small" type="primary">{{ scope.row.type || '未分类' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="floors" label="楼层数" width="100" align="center">
              <template #default="scope">
                {{ scope.row.floors || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="开放时间" width="150">
              <template #default="scope">
                <span v-if="scope.row.openTime && scope.row.closeTime">
                  {{ scope.row.openTime }} - {{ scope.row.closeTime }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewBuildingDetail(scope.row)">
                  <el-icon><View /></el-icon>
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 建筑详情对话框 -->
    <el-dialog v-model="dialogVisible" title="建筑详情" width="60%">
      <div v-if="currentBuilding">
        <p><strong>名称：</strong>{{ currentBuilding.name }}</p>
        <p><strong>类型：</strong>{{ currentBuilding.type }}</p>
        <p><strong>楼层数：</strong>{{ currentBuilding.floors }}</p>
        <p><strong>开放时间：</strong>{{ currentBuilding.openTime }} - {{ currentBuilding.closeTime }}</p>
        <p><strong>描述：</strong>{{ currentBuilding.description }}</p>
        
        <h4>建筑内设施</h4>
        <el-table :data="facilities" style="width: 100%">
          <el-table-column prop="name" label="设施名称" />
          <el-table-column prop="type" label="类型" />
          <el-table-column prop="locationDesc" label="位置" />
          <el-table-column prop="openTime" label="开放时间" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { Location, View } from '@element-plus/icons-vue'
import { campusAPI, buildingAPI, facilityAPI } from '../api'
import { ElMessage } from 'element-plus'

export default {
  name: 'CampusView',
  components: {
    Location,
    View
  },
  setup() {
    const campuses = ref([])
    const buildings = ref([])
    const selectedCampus = ref(null)
    const loading = ref(false)
    const dialogVisible = ref(false)
    const currentBuilding = ref(null)
    const facilities = ref([])

    const loadCampuses = async () => {
      try {
        console.log('开始加载校区数据...')
        const data = await campusAPI.getAllCampuses()
        console.log('校区数据:', data)
        campuses.value = data
        
        if (data.length === 0) {
          ElMessage.warning('暂无校区数据，请联系管理员')
        }
      } catch (error) {
        console.error('加载校区失败:', error)
        ElMessage.error('加载校区失败，请检查后端服务是否正常运行')
      }
    }

    const handleCampusSelect = async (index) => {
      const campusId = parseInt(index)
      selectedCampus.value = campuses.value.find(c => c.campusId === campusId)
      loading.value = true
      
      console.log('选择校区ID:', campusId)
      
      try {
        const data = await buildingAPI.getBuildingsByCampus(campusId)
        console.log('建筑数据:', data)
        buildings.value = data
        
        if (data.length === 0) {
          ElMessage.info('该校区暂无建筑数据')
        }
      } catch (error) {
        console.error('加载建筑失败:', error)
        ElMessage.error('加载建筑失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    const viewBuildingDetail = async (building) => {
      currentBuilding.value = building
      dialogVisible.value = true
      
      try {
        facilities.value = await facilityAPI.getFacilitiesByBuilding(building.buildingId)
      } catch (error) {
        ElMessage.error('加载设施失败')
      }
    }

    onMounted(() => {
      loadCampuses()
    })

    return {
      campuses,
      buildings,
      selectedCampus,
      loading,
      dialogVisible,
      currentBuilding,
      facilities,
      handleCampusSelect,
      viewBuildingDetail
    }
  }
}
</script>

<style scoped>
.campus-view {
  max-width: 1200px;
  margin: 0 auto;
}

h2 {
  color: #409EFF;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 26px;
  font-weight: bold;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
  color: #303133;
}

.el-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: #303133 !important;
  font-weight: 500;
}

.el-menu-item .el-icon {
  font-size: 16px;
  color: #409EFF;
}

.el-menu-item:hover {
  background-color: #ecf5ff !important;
  color: #409EFF !important;
}

.el-menu-item.is-active {
  background-color: #ecf5ff !important;
  color: #409EFF !important;
  font-weight: bold;
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

:deep(.el-button) {
  font-size: 13px;
}

/* 对话框样式 */
:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

:deep(.el-dialog__body) {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
}

h4 {
  margin-top: 20px;
  margin-bottom: 15px;
  color: #303133;
  font-size: 16px;
  font-weight: bold;
}

p {
  margin: 10px 0;
  font-size: 14px;
  line-height: 1.6;
}

p strong {
  color: #303133;
  font-weight: 600;
}
</style>
