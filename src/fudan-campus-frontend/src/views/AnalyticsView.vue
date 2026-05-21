<template>
  <div class="analytics-view">
    <h2>📊 智能查询分析与趋势统计</h2>
    
    <!-- 统计概览卡片 -->
    <el-row :gutter="20" style="margin-bottom: 30px;">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon size="40" color="#409EFF"><DataLine /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalQueries || 0 }}</div>
              <div class="stat-label">总查询次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon size="40" color="#67C23A"><TrendCharts /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalBuildings || 0 }}</div>
              <div class="stat-label">校园建筑</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon size="40" color="#E6A23C"><CoffeeCup /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalFacilities || 0 }}</div>
              <div class="stat-label">校园设施</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon size="40" color="#F56C6C"><Calendar /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalEvents || 0 }}</div>
              <div class="stat-label">近期活动</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab">
      <!-- 查询趋势分析 -->
      <el-tab-pane label="📈 查询趋势" name="trend">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>每日查询趋势（最近{{ trendDays }}天）</span>
              <el-radio-group v-model="trendDays" @change="loadDailyTrend" size="small">
                <el-radio-button :label="7">7天</el-radio-button>
                <el-radio-button :label="15">15天</el-radio-button>
                <el-radio-button :label="30">30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          
          <div ref="trendChartRef" style="height: 400px;"></div>
        </el-card>
      </el-tab-pane>

      <!-- 热门查询类别 -->
      <el-tab-pane label="🔥 热门查询" name="popular">
        <el-card>
          <template #header>
            <span>热门查询类别排行榜</span>
          </template>
          
          <el-table :data="popularCategories" stripe>
            <el-table-column type="index" label="排名" width="80" align="center" />
            <el-table-column prop="category" label="查询类别" min-width="150">
              <template #default="scope">
                <el-tag :type="getCategoryTagType(scope.$index)">{{ scope.row.category }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="queryCount" label="查询次数" width="120" align="center" />
            <el-table-column prop="uniqueUsers" label="独立用户数" width="120" align="center" />
            <el-table-column prop="percentage" label="占比" width="100" align="center">
              <template #default="scope">
                {{ scope.row.percentage }}%
              </template>
            </el-table-column>
            <el-table-column label="热度条" min-width="200">
              <template #default="scope">
                <el-progress 
                  :percentage="parseFloat(scope.row.percentage)" 
                  :color="getProgressColor(scope.$index)"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 活跃用户排行 -->
      <el-tab-pane label="👥 活跃用户" name="users">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>活跃用户TOP 10</span>
              <el-tag type="info">基于查询历史</el-tag>
            </div>
          </template>
          
          <el-table :data="activeUsers" stripe>
            <el-table-column type="index" label="排名" width="80" align="center" />
            <el-table-column prop="username" label="用户名" width="150" />
            <el-table-column prop="name" label="姓名" width="120" />
            <el-table-column prop="totalQueries" label="查询次数" width="120" align="center">
              <template #default="scope">
                <el-tag type="success">{{ scope.row.totalQueries }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="categoriesExplored" label="探索类别数" width="130" align="center" />
            <el-table-column label="活跃度评分" min-width="150">
              <template #default="scope">
                <el-rate 
                  v-model="scope.row.rating" 
                  disabled 
                  show-score 
                  text-color="#ff9900"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 校区设施分布 -->
      <el-tab-pane label="🏫 校区设施" name="campus">
        <el-card>
          <template #header>
            <span>各校区设施分布统计</span>
          </template>
          
          <el-table :data="campusStats" stripe>
            <el-table-column prop="campusName" label="校区名称" min-width="150" />
            <el-table-column prop="buildingCount" label="建筑数量" width="120" align="center">
              <template #default="scope">
                <el-tag type="primary">{{ scope.row.buildingCount }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="facilityCount" label="设施数量" width="120" align="center">
              <template #default="scope">
                <el-tag type="success">{{ scope.row.facilityCount }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="设施密度" min-width="200">
              <template #default="scope">
                <el-progress 
                  :percentage="calculateDensityPercentage(scope.row)" 
                  :format="() => `${scope.row.facilityCount} 个设施`"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import { DataLine, TrendCharts, CoffeeCup, Calendar } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

export default {
  name: 'AnalyticsView',
  components: {
    DataLine,
    TrendCharts,
    CoffeeCup,
    Calendar
  },
  setup() {
    const activeTab = ref('trend')
    const trendDays = ref(7)
    const trendChartRef = ref(null)
    let trendChart = null
    
    const overview = ref({})
    const popularCategories = ref([])
    const activeUsers = ref([])
    const campusStats = ref([])
    const dailyTrend = ref([])

    // 加载综合统计概览
    const loadOverview = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/statistics/overview')
        overview.value = await response.json()
      } catch (error) {
        console.error('加载概览数据失败:', error)
      }
    }

    // 加载每日查询趋势
    const loadDailyTrend = async () => {
      try {
        const response = await fetch(`http://localhost:8080/api/statistics/daily-trend?days=${trendDays.value}`)
        dailyTrend.value = await response.json()
        
        // 渲染图表
        await nextTick()
        renderTrendChart()
      } catch (error) {
        console.error('加载趋势数据失败:', error)
        ElMessage.error('加载趋势数据失败')
      }
    }

    // 渲染趋势图表
    const renderTrendChart = () => {
      if (!trendChartRef.value) return
      
      if (trendChart) {
        trendChart.dispose()
      }
      
      trendChart = echarts.init(trendChartRef.value)
      
      const dates = dailyTrend.value.map(item => item.date).reverse()
      const counts = dailyTrend.value.map(item => item.count).reverse()
      const users = dailyTrend.value.map(item => item.uniqueUsers).reverse()
      
      const option = {
        title: {
          text: '查询趋势分析',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          }
        },
        legend: {
          data: ['查询次数', '独立用户数'],
          top: 30
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: dates
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '查询次数',
            type: 'line',
            smooth: true,
            data: counts,
            areaStyle: {
              opacity: 0.3
            },
            itemStyle: {
              color: '#409EFF'
            }
          },
          {
            name: '独立用户数',
            type: 'line',
            smooth: true,
            data: users,
            areaStyle: {
              opacity: 0.3
            },
            itemStyle: {
              color: '#67C23A'
            }
          }
        ]
      }
      
      trendChart.setOption(option)
    }

    // 加载热门查询类别
    const loadPopularCategories = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/statistics/popular-categories')
        popularCategories.value = await response.json()
      } catch (error) {
        console.error('加载热门类别失败:', error)
      }
    }

    // 加载活跃用户
    const loadActiveUsers = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/statistics/active-users?limit=10')
        const users = await response.json()
        
        // 计算活跃度评分（满分5星）
        activeUsers.value = users.map(user => ({
          ...user,
          rating: Math.min(5, Math.ceil(user.totalQueries / 10))
        }))
      } catch (error) {
        console.error('加载活跃用户失败:', error)
      }
    }

    // 加载校区设施统计
    const loadCampusStats = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/statistics/campus-facility-popularity')
        campusStats.value = await response.json()
      } catch (error) {
        console.error('加载校区统计失败:', error)
      }
    }

    // 获取类别标签类型
    const getCategoryTagType = (index) => {
      const types = ['danger', 'warning', 'success', 'info', '']
      return types[index % types.length]
    }

    // 获取进度条颜色
    const getProgressColor = (index) => {
      const colors = ['#f56c6c', '#e6a23c', '#67c23a', '#409eff', '#909399']
      return colors[index % colors.length]
    }

    // 计算设施密度百分比
    const calculateDensityPercentage = (row) => {
      const maxFacilities = Math.max(...campusStats.value.map(s => s.facilityCount))
      return maxFacilities > 0 ? Math.round((row.facilityCount / maxFacilities) * 100) : 0
    }

    onMounted(() => {
      loadOverview()
      loadDailyTrend()
      loadPopularCategories()
      loadActiveUsers()
      loadCampusStats()
      
      // 监听窗口大小变化，重新渲染图表
      window.addEventListener('resize', () => {
        if (trendChart) {
          trendChart.resize()
        }
      })
    })

    return {
      activeTab,
      trendDays,
      trendChartRef,
      overview,
      popularCategories,
      activeUsers,
      campusStats,
      dailyTrend,
      loadDailyTrend,
      getCategoryTagType,
      getProgressColor,
      calculateDensityPercentage
    }
  }
}
</script>

<style scoped>
.analytics-view {
  max-width: 1400px;
  margin: 0 auto;
}

h2 {
  color: #409EFF;
  margin-bottom: 20px;
  font-size: 26px;
  font-weight: bold;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-progress__text) {
  font-size: 12px !important;
}
</style>
