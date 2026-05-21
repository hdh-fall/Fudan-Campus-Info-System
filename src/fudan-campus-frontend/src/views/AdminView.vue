<template>
  <div class="admin-view">
    <!-- 权限检查 -->
    <el-alert
      v-if="!isAdmin"
      title="⚠️ 权限不足"
      type="error"
      description="只有管理员才能访问管理后台。请切换到管理员账号后再试。"
      :closable="false"
      show-icon
      style="margin-bottom: 20px;"
    >
      <template #default>
        <el-button type="primary" @click="$emit('navigate', 'user')">去切换账号</el-button>
      </template>
    </el-alert>

    <div v-else>
      <h2>🔧 管理后台</h2>
    
    <el-tabs v-model="activeTab">
      <!-- 建筑管理 -->
      <el-tab-pane name="building">
        <template #label>
          <span>🏢 建筑管理</span>
        </template>
        
        <el-card>
          <template #header>
            <div class="card-header">
              <span>建筑列表</span>
              <el-tag size="small" type="info">{{ buildings.length }} 栋建筑</el-tag>
            </div>
          </template>
          
          <div style="margin-bottom: 20px;">
            <el-button type="primary" @click="showAddDialog('building')" style="margin-right: 10px;">
              <el-icon><Plus /></el-icon>
              添加建筑
            </el-button>
            <el-upload
              action="http://localhost:8080/api/import/buildings"
              :on-success="handleImportSuccess"
              :on-error="handleImportError"
              accept=".csv"
              style="display: inline-block; margin-right: 10px;"
            >
              <el-button type="success">
                <el-icon><Upload /></el-icon>
                CSV导入
              </el-button>
            </el-upload>
            <el-button type="info" @click="loadData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
          
          <el-empty v-if="buildings.length === 0 && !loading" description="暂无建筑数据" :image-size="100" />
          
          <el-table v-else :data="buildings" style="width: 100%" v-loading="loading" stripe>
            <el-table-column prop="name" label="建筑名称" min-width="150" />
            <el-table-column prop="type" label="类型" width="120">
              <template #default="scope">
                <el-tag size="small" type="primary">{{ scope.row.type || '未分类' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="campus.name" label="所属校区" min-width="120">
              <template #default="scope">
                {{ scope.row.campus?.name || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="floors" label="楼层数" width="100" align="center">
              <template #default="scope">
                {{ scope.row.floors || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleEdit('building', scope.row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="handleDelete('building', scope.row.buildingId)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 设施管理 -->
      <el-tab-pane name="facility">
        <template #label>
          <span>☕ 设施管理</span>
        </template>
        
        <el-card>
          <template #header>
            <div class="card-header">
              <span>设施列表</span>
              <el-tag size="small" type="info">{{ facilities.length }} 个设施</el-tag>
            </div>
          </template>
          
          <div style="margin-bottom: 20px;">
            <el-button type="primary" @click="showAddDialog('facility')" style="margin-right: 10px;">
              <el-icon><Plus /></el-icon>
              添加设施
            </el-button>
            <el-upload
              action="http://localhost:8080/api/import/facilities"
              :on-success="handleImportSuccess"
              :on-error="handleImportError"
              accept=".csv"
              style="display: inline-block; margin-right: 10px;"
            >
              <el-button type="success">
                <el-icon><Upload /></el-icon>
                CSV导入
              </el-button>
            </el-upload>
            <el-button type="info" @click="loadData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
          
          <el-empty v-if="facilities.length === 0 && !loading" description="暂无设施数据" :image-size="100" />
          
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
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleEdit('facility', scope.row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="handleDelete('facility', scope.row.facilityId)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 课程管理 -->
      <el-tab-pane name="course">
        <template #label>
          <span>📚 课程管理</span>
        </template>
        
        <el-card>
          <template #header>
            <div class="card-header">
              <span>课程列表</span>
              <el-tag size="small" type="info">{{ courses.length }} 门课程</el-tag>
            </div>
          </template>
          
          <div style="margin-bottom: 20px;">
            <el-button type="primary" @click="showAddDialog('course')" style="margin-right: 10px;">
              <el-icon><Plus /></el-icon>
              添加课程
            </el-button>
            <el-upload
              action="http://localhost:8080/api/import/courses"
              :on-success="handleImportSuccess"
              :on-error="handleImportError"
              accept=".csv"
              style="display: inline-block; margin-right: 10px;"
            >
              <el-button type="success">
                <el-icon><Upload /></el-icon>
                CSV导入
              </el-button>
            </el-upload>
            <el-button type="info" @click="loadData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
          
          <el-empty v-if="courses.length === 0 && !loading" description="暂无课程数据" :image-size="100" />
          
          <el-table v-else :data="courses" style="width: 100%" v-loading="loading" stripe>
            <el-table-column prop="name" label="课程名称" min-width="180" />
            <el-table-column prop="department.name" label="开课院系" min-width="150">
              <template #default="scope">
                {{ scope.row.department?.name || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="credits" label="学分" width="80" align="center">
              <template #default="scope">
                {{ scope.row.credits || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleEdit('course', scope.row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="handleDelete('course', scope.row.courseId)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 教师管理 -->
      <el-tab-pane name="teacher">
        <template #label>
          <span>👨‍🏫 教师管理</span>
        </template>
        
        <el-card>
          <template #header>
            <div class="card-header">
              <span>教师列表</span>
              <el-tag size="small" type="info">{{ teachers.length }} 位教师</el-tag>
            </div>
          </template>
          
          <div style="margin-bottom: 20px;">
            <el-button type="primary" @click="showAddDialog('teacher')" style="margin-right: 10px;">
              <el-icon><Plus /></el-icon>
              添加教师
            </el-button>
            <el-upload
              action="http://localhost:8080/api/import/teachers"
              :on-success="handleImportSuccess"
              :on-error="handleImportError"
              accept=".csv"
              style="display: inline-block; margin-right: 10px;"
            >
              <el-button type="success">
                <el-icon><Upload /></el-icon>
                CSV导入
              </el-button>
            </el-upload>
            <el-button type="info" @click="loadData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
          
          <el-empty v-if="teachers.length === 0 && !loading" description="暂无教师数据" :image-size="100" />
          
          <el-table v-else :data="teachers" style="width: 100%" v-loading="loading" stripe>
            <el-table-column prop="name" label="教师姓名" min-width="120" />
            <el-table-column prop="department.name" label="所属院系" min-width="150">
              <template #default="scope">
                {{ scope.row.department?.name || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="title" label="职称" width="120">
              <template #default="scope">
                <el-tag size="small" type="warning">{{ scope.row.title || '未评定' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleEdit('teacher', scope.row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="handleDelete('teacher', scope.row.teacherId)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 活动管理 -->
      <el-tab-pane name="event">
        <template #label>
          <span>🎉 活动管理</span>
        </template>
        
        <el-card>
          <template #header>
            <div class="card-header">
              <span>活动列表</span>
              <el-tag size="small" type="info">{{ events.length }} 个活动</el-tag>
            </div>
          </template>
          
          <div style="margin-bottom: 20px;">
            <el-button type="primary" @click="showAddDialog('event')" style="margin-right: 10px;">
              <el-icon><Plus /></el-icon>
              添加活动
            </el-button>
            <el-upload
              action="http://localhost:8080/api/import/events"
              :on-success="handleImportSuccess"
              :on-error="handleImportError"
              accept=".csv"
              style="display: inline-block; margin-right: 10px;"
            >
              <el-button type="success">
                <el-icon><Upload /></el-icon>
                CSV导入
              </el-button>
            </el-upload>
            <el-button type="info" @click="loadData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
          
          <el-empty v-if="events.length === 0 && !loading" description="暂无活动数据" :image-size="100" />
          
          <el-table v-else :data="events" style="width: 100%" v-loading="loading" stripe>
            <el-table-column prop="name" label="活动名称" min-width="180" />
            <el-table-column prop="eventTime" label="活动时间" width="160">
              <template #default="scope">
                <el-tag size="small" type="info">{{ formatDateTime(scope.row.eventTime) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="organizer" label="主办方" min-width="150" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleEdit('event', scope.row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="handleDelete('event', scope.row.eventId)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="`编辑${getEntityName(editType)}`" width="600px">
      <el-form :model="editForm" label-width="100px">
        <!-- 建筑编辑表单 -->
        <template v-if="editType === 'building'">
          <el-form-item label="建筑名称">
            <el-input v-model="editForm.name" placeholder="请输入建筑名称" />
          </el-form-item>
          <el-form-item label="类型">
            <el-input v-model="editForm.type" placeholder="例如：教学楼、实验楼" />
          </el-form-item>
          <el-form-item label="所属校区">
            <el-select v-model="editForm.campusId" placeholder="请选择校区" style="width: 100%;" clearable>
              <el-option
                v-for="campus in campuses"
                :key="campus.campusId"
                :label="campus.name"
                :value="campus.campusId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="楼层数">
            <el-input-number v-model="editForm.floors" :min="1" :max="100" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
          </el-form-item>
        </template>

        <!-- 设施编辑表单 -->
        <template v-else-if="editType === 'facility'">
          <el-form-item label="设施名称">
            <el-input v-model="editForm.name" placeholder="请输入设施名称" />
          </el-form-item>
          <el-form-item label="类型">
            <el-input v-model="editForm.type" placeholder="例如：食堂、咖啡厅" />
          </el-form-item>
          <el-form-item label="所属建筑">
            <el-select v-model="editForm.buildingId" placeholder="请选择建筑" style="width: 100%;" clearable>
              <el-option
                v-for="building in buildingsList"
                :key="building.buildingId"
                :label="building.name"
                :value="building.buildingId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="容量">
            <el-input-number v-model="editForm.capacity" :min="1" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="editForm.contact" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item label="位置描述">
            <el-input v-model="editForm.locationDesc" placeholder="例如：一楼、二楼" />
          </el-form-item>
        </template>

        <!-- 课程编辑表单 -->
        <template v-else-if="editType === 'course'">
          <el-form-item label="课程名称">
            <el-input v-model="editForm.name" placeholder="请输入课程名称" />
          </el-form-item>
          <el-form-item label="开课院系">
            <el-select v-model="editForm.departmentId" placeholder="请选择院系" style="width: 100%;" clearable>
              <el-option
                v-for="dept in departments"
                :key="dept.departmentId"
                :label="dept.name"
                :value="dept.departmentId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="学分">
            <el-input-number v-model="editForm.credits" :min="0" :max="10" :step="0.5" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="开课学期">
            <el-input v-model="editForm.semesterOffered" placeholder="例如：2024-2025-1" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入课程描述" />
          </el-form-item>
        </template>

        <!-- 教师编辑表单 -->
        <template v-else-if="editType === 'teacher'">
          <el-form-item label="教师姓名">
            <el-input v-model="editForm.name" placeholder="请输入教师姓名" />
          </el-form-item>
          <el-form-item label="所属院系">
            <el-select v-model="editForm.departmentId" placeholder="请选择院系" style="width: 100%;" clearable>
              <el-option
                v-for="dept in departments"
                :key="dept.departmentId"
                :label="dept.name"
                :value="dept.departmentId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="职称">
            <el-input v-model="editForm.title" placeholder="例如：教授、副教授" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="editForm.email" placeholder="请输入邮箱地址" />
          </el-form-item>
          <el-form-item label="电话">
            <el-input v-model="editForm.phone" placeholder="请输入联系电话" />
          </el-form-item>
        </template>

        <!-- 活动编辑表单 -->
        <template v-else-if="editType === 'event'">
          <el-form-item label="活动名称">
            <el-input v-model="editForm.name" placeholder="请输入活动名称" />
          </el-form-item>
          <el-form-item label="主办方">
            <el-input v-model="editForm.organizer" placeholder="请输入主办方" />
          </el-form-item>
          <el-form-item label="类别">
            <el-input v-model="editForm.category" placeholder="例如：学术讲座、文艺活动" />
          </el-form-item>
          <el-form-item label="地点">
            <el-input v-model="editForm.locationDesc" placeholder="请输入活动地点" />
          </el-form-item>
          <el-form-item label="所属校区">
            <el-select v-model="editForm.campusId" placeholder="请选择校区" style="width: 100%;" clearable>
              <el-option
                v-for="campus in campuses"
                :key="campus.campusId"
                :label="campus.name"
                :value="campus.campusId"
              />
            </el-select>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmEdit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加对话框 -->
    <el-dialog v-model="addDialogVisible" :title="`添加${getEntityName(addType)}`" width="600px">
      <el-form :model="addForm" label-width="100px">
        <!-- 建筑添加表单 -->
        <template v-if="addType === 'building'">
          <el-form-item label="建筑名称" required>
            <el-input v-model="addForm.name" placeholder="请输入建筑名称" />
          </el-form-item>
          <el-form-item label="类型">
            <el-input v-model="addForm.type" placeholder="例如：教学楼、实验楼" />
          </el-form-item>
          <el-form-item label="所属校区">
            <el-select v-model="addForm.campusId" placeholder="请选择校区" style="width: 100%;" clearable>
              <el-option
                v-for="campus in campuses"
                :key="campus.campusId"
                :label="campus.name"
                :value="campus.campusId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="楼层数">
            <el-input-number v-model="addForm.floors" :min="1" :max="100" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="addForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
          </el-form-item>
        </template>

        <!-- 设施添加表单 -->
        <template v-else-if="addType === 'facility'">
          <el-form-item label="设施名称" required>
            <el-input v-model="addForm.name" placeholder="请输入设施名称" />
          </el-form-item>
          <el-form-item label="类型" required>
            <el-input v-model="addForm.type" placeholder="例如：食堂、咖啡厅" />
          </el-form-item>
          <el-form-item label="所属建筑" required>
            <el-select v-model="addForm.buildingId" placeholder="请选择建筑" style="width: 100%;" clearable>
              <el-option
                v-for="building in buildingsList"
                :key="building.buildingId"
                :label="building.name"
                :value="building.buildingId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="容量">
            <el-input-number v-model="addForm.capacity" :min="1" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="addForm.contact" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item label="位置描述">
            <el-input v-model="addForm.locationDesc" placeholder="例如：一楼、二楼" />
          </el-form-item>
        </template>

        <!-- 课程添加表单 -->
        <template v-else-if="addType === 'course'">
          <el-form-item label="课程名称" required>
            <el-input v-model="addForm.name" placeholder="请输入课程名称" />
          </el-form-item>
          <el-form-item label="开课院系">
            <el-select v-model="addForm.departmentId" placeholder="请选择院系" style="width: 100%;" clearable>
              <el-option
                v-for="dept in departments"
                :key="dept.departmentId"
                :label="dept.name"
                :value="dept.departmentId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="学分">
            <el-input-number v-model="addForm.credits" :min="0" :max="10" :step="0.5" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="开课学期">
            <el-input v-model="addForm.semesterOffered" placeholder="例如：2024-2025-1" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="addForm.description" type="textarea" :rows="3" placeholder="请输入课程描述" />
          </el-form-item>
        </template>

        <!-- 教师添加表单 -->
        <template v-else-if="addType === 'teacher'">
          <el-form-item label="教师姓名" required>
            <el-input v-model="addForm.name" placeholder="请输入教师姓名" />
          </el-form-item>
          <el-form-item label="所属院系">
            <el-select v-model="addForm.departmentId" placeholder="请选择院系" style="width: 100%;" clearable>
              <el-option
                v-for="dept in departments"
                :key="dept.departmentId"
                :label="dept.name"
                :value="dept.departmentId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="职称">
            <el-input v-model="addForm.title" placeholder="例如：教授、副教授" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="addForm.email" placeholder="请输入邮箱地址" />
          </el-form-item>
          <el-form-item label="电话">
            <el-input v-model="addForm.phone" placeholder="请输入联系电话" />
          </el-form-item>
        </template>

        <!-- 活动添加表单 -->
        <template v-else-if="addType === 'event'">
          <el-form-item label="活动名称" required>
            <el-input v-model="addForm.name" placeholder="请输入活动名称" />
          </el-form-item>
          <el-form-item label="活动时间" required>
            <el-date-picker
              v-model="addForm.eventTime"
              type="datetime"
              placeholder="选择日期时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%;"
            />
          </el-form-item>
          <el-form-item label="主办方">
            <el-input v-model="addForm.organizer" placeholder="请输入主办方" />
          </el-form-item>
          <el-form-item label="类别">
            <el-input v-model="addForm.category" placeholder="例如：学术讲座、文艺活动" />
          </el-form-item>
          <el-form-item label="地点">
            <el-input v-model="addForm.locationDesc" placeholder="请输入活动地点" />
          </el-form-item>
          <el-form-item label="所属校区">
            <el-select v-model="addForm.campusId" placeholder="请选择校区" style="width: 100%;" clearable>
              <el-option
                v-for="campus in campuses"
                :key="campus.campusId"
                :label="campus.name"
                :value="campus.campusId"
              />
            </el-select>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { Plus, Delete, Edit, Upload, Refresh } from '@element-plus/icons-vue'
import { buildingAPI, facilityAPI, courseAPI, teacherAPI, eventAPI, campusAPI, departmentAPI } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'AdminView',
  components: {
    Plus,
    Delete,
    Edit,
    Upload,
    Refresh
  },
  setup() {
    const activeTab = ref('building')
    const buildings = ref([])
    const facilities = ref([])
    const courses = ref([])
    const teachers = ref([])
    const events = ref([])
    const loading = ref(false)
    
    // 权限检查
    const currentUser = ref(null)
    const isAdmin = computed(() => {
      return currentUser.value && currentUser.value.role === 'admin'
    })
    
    const goToUserCenter = () => {
      // 直接修改localStorage中的activeMenu，然后刷新页面
      // 或者使用window.location直接跳转
      window.dispatchEvent(new CustomEvent('navigate', { detail: 'user' }))
      // 备用方案：直接设置hash
      setTimeout(() => {
        window.location.href = window.location.pathname + '#user'
      }, 100)
    }
    
    // 编辑相关
    const editDialogVisible = ref(false)
    const editType = ref('')
    const editForm = ref({})
    const editRowId = ref(null)
    
    // 添加相关
    const addDialogVisible = ref(false)
    const addType = ref('')
    const addForm = ref({})
    
    // 关联数据
    const campuses = ref([])
    const buildingsList = ref([])
    const departments = ref([])

    const loadData = async () => {
      loading.value = true
      try {
        console.log('加载管理数据...')
        buildings.value = await buildingAPI.searchBuildings('')
        facilities.value = await facilityAPI.searchFacilities('')
        courses.value = await courseAPI.searchCourses('')
        teachers.value = await teacherAPI.searchTeachers('')
        events.value = await eventAPI.searchEvents('')
        console.log('数据加载完成')
        ElMessage.success('数据已刷新')
      } catch (error) {
        console.error('加载数据失败:', error)
        ElMessage.error('加载数据失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
    
    // 加载关联数据（校区、建筑、院系）
    const loadRelatedData = async () => {
      try {
        const [campusesData, buildingsData, departmentsData] = await Promise.all([
          campusAPI.getAllCampuses(),
          buildingAPI.searchBuildings(''),
          departmentAPI.getAllDepartments()
        ])
        campuses.value = campusesData || []
        buildingsList.value = buildingsData || []
        departments.value = departmentsData || []
        console.log('关联数据加载完成', { campuses: campuses.value, buildings: buildingsList.value, departments: departments.value })
      } catch (error) {
        console.error('加载关联数据失败:', error)
      }
    }

    const handleDelete = async (type, id) => {
      try {
        await ElMessageBox.confirm('确定要删除吗？此操作不可恢复！', '警告', {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        console.log('删除', type, 'ID:', id)
        
        switch(type) {
          case 'building':
            await buildingAPI.deleteBuilding(id)
            break
          case 'facility':
            await facilityAPI.deleteFacility(id)
            break
          case 'course':
            await courseAPI.deleteCourse(id)
            break
          case 'teacher':
            await teacherAPI.deleteTeacher(id)
            break
          case 'event':
            await eventAPI.deleteEvent(id)
            break
        }
        
        ElMessage.success('删除成功')
        loadData()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          ElMessage.error('删除失败，请稍后重试')
        }
      }
    }

    const handleEdit = (type, row) => {
      console.log('编辑类型:', type)
      console.log('编辑行数据:', row)
      editType.value = type
      // 复制所有字段，避免显示undefined
      editForm.value = { ...row }
      
      // 处理关联对象的ID，方便下拉框选择
      if (type === 'building' && row.campus) {
        editForm.value.campusId = row.campus.campusId
      } else if (type === 'facility' && row.building) {
        editForm.value.buildingId = row.building.buildingId
      } else if (type === 'course' && row.department) {
        editForm.value.departmentId = row.department.departmentId
      } else if (type === 'teacher' && row.department) {
        editForm.value.departmentId = row.department.departmentId
      } else if (type === 'event' && row.campus) {
        editForm.value.campusId = row.campus.campusId
      }
      
      console.log('编辑表单数据:', editForm.value)
      editRowId.value = getRowId(type, row)
      editDialogVisible.value = true
    }

    const confirmEdit = async () => {
      if (!editForm.value.name || editForm.value.name.trim() === '') {
        ElMessage.warning('名称不能为空')
        return
      }

      try {
        let updatedData = {}
        
        // 根据不同实体类型构建更新数据
        switch(editType.value) {
          case 'building':
            updatedData = {
              buildingId: editRowId.value,
              name: editForm.value.name,
              type: editForm.value.type,
              campus: editForm.value.campusId ? { campusId: editForm.value.campusId } : null,
              floors: editForm.value.floors,
              description: editForm.value.description
            }
            break
          case 'facility':
            updatedData = {
              facilityId: editRowId.value,
              name: editForm.value.name,
              type: editForm.value.type,
              building: editForm.value.buildingId ? { buildingId: editForm.value.buildingId } : null,
              capacity: editForm.value.capacity,
              contact: editForm.value.contact,
              locationDesc: editForm.value.locationDesc
            }
            break
          case 'course':
            updatedData = {
              courseId: editRowId.value,
              name: editForm.value.name,
              department: editForm.value.departmentId ? { departmentId: editForm.value.departmentId } : null,
              credits: editForm.value.credits,
              semesterOffered: editForm.value.semesterOffered,
              description: editForm.value.description
            }
            break
          case 'teacher':
            updatedData = {
              teacherId: editRowId.value,
              name: editForm.value.name,
              department: editForm.value.departmentId ? { departmentId: editForm.value.departmentId } : null,
              title: editForm.value.title,
              email: editForm.value.email,
              phone: editForm.value.phone
            }
            break
          case 'event':
            updatedData = {
              eventId: editRowId.value,
              name: editForm.value.name,
              organizer: editForm.value.organizer,
              category: editForm.value.category,
              locationDesc: editForm.value.locationDesc,
              campus: editForm.value.campusId ? { campusId: editForm.value.campusId } : null
            }
            break
        }
        
        // 调用对应的API
        switch(editType.value) {
          case 'building':
            await buildingAPI.updateBuilding(editRowId.value, updatedData)
            break
          case 'facility':
            await facilityAPI.updateFacility(editRowId.value, updatedData)
            break
          case 'course':
            await courseAPI.updateCourse(editRowId.value, updatedData)
            break
          case 'teacher':
            await teacherAPI.updateTeacher(editRowId.value, updatedData)
            break
          case 'event':
            await eventAPI.updateEvent(editRowId.value, updatedData)
            break
        }
        
        ElMessage.success('修改成功')
        editDialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('修改失败:', error)
        ElMessage.error('修改失败，请稍后重试')
      }
    }

    const showAddDialog = (type) => {
      console.log('添加类型:', type)
      addType.value = type
      // 初始化表单
      addForm.value = {}
      console.log('添加表单数据:', addForm.value)
      addDialogVisible.value = true
    }

    const confirmAdd = async () => {
      // 验证必填字段
      if (!addForm.value.name || addForm.value.name.trim() === '') {
        ElMessage.warning('名称不能为空')
        return
      }

      try {
        let newData = {}
        
        // 根据不同实体类型构建数据
        switch(addType.value) {
          case 'building':
            newData = {
              name: addForm.value.name,
              type: addForm.value.type || '',
              campus: addForm.value.campusId ? { campusId: addForm.value.campusId } : null,
              floors: addForm.value.floors || null,
              description: addForm.value.description || ''
            }
            await buildingAPI.createBuilding(newData)
            break
          case 'facility':
            if (!addForm.value.buildingId) {
              ElMessage.warning('建筑ID不能为空')
              return
            }
            newData = {
              name: addForm.value.name,
              type: addForm.value.type,
              building: { buildingId: addForm.value.buildingId },
              capacity: addForm.value.capacity || null,
              contact: addForm.value.contact || '',
              locationDesc: addForm.value.locationDesc || ''
            }
            await facilityAPI.createFacility(newData)
            break
          case 'course':
            newData = {
              name: addForm.value.name,
              department: addForm.value.departmentId ? { departmentId: addForm.value.departmentId } : null,
              credits: addForm.value.credits || null,
              semesterOffered: addForm.value.semesterOffered || '',
              description: addForm.value.description || ''
            }
            await courseAPI.createCourse(newData)
            break
          case 'teacher':
            newData = {
              name: addForm.value.name,
              department: addForm.value.departmentId ? { departmentId: addForm.value.departmentId } : null,
              title: addForm.value.title || '',
              email: addForm.value.email || '',
              phone: addForm.value.phone || ''
            }
            await teacherAPI.createTeacher(newData)
            break
          case 'event':
            if (!addForm.value.eventTime) {
              ElMessage.warning('活动时间不能为空')
              return
            }
            newData = {
              name: addForm.value.name,
              eventTime: addForm.value.eventTime,
              organizer: addForm.value.organizer || '',
              category: addForm.value.category || '',
              locationDesc: addForm.value.locationDesc || '',
              campus: addForm.value.campusId ? { campusId: addForm.value.campusId } : null
            }
            await eventAPI.createEvent(newData)
            break
        }
        
        ElMessage.success('添加成功')
        addDialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('添加失败:', error)
        ElMessage.error('添加失败，请检查数据格式')
      }
    }

    const handleImportSuccess = (response) => {
      console.log('导入成功:', response)
      if (response.success) {
        ElMessage.success(response.message || '导入成功')
        loadData()
      } else {
        ElMessage.error(response.message || '导入失败')
      }
    }

    const handleImportError = (error) => {
      console.error('导入失败:', error)
      ElMessage.error('文件上传失败，请检查文件格式')
    }

    const getEntityName = (type) => {
      const names = {
        building: '建筑',
        facility: '设施',
        course: '课程',
        teacher: '教师',
        event: '活动'
      }
      return names[type] || type
    }

    const getRowId = (type, row) => {
      switch(type) {
        case 'building': return row.buildingId
        case 'facility': return row.facilityId
        case 'course': return row.courseId
        case 'teacher': return row.teacherId
        case 'event': return row.eventId
        default: return null
      }
    }

    const formatDateTime = (datetime) => {
      if (!datetime) return ''
      const date = new Date(datetime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    onMounted(() => {
      // 获取当前用户信息
      const savedUser = localStorage.getItem('currentUser')
      if (savedUser) {
        currentUser.value = JSON.parse(savedUser)
      }
      
      // 只有管理员才加载数据
      if (isAdmin.value) {
        loadRelatedData() // 先加载关联数据（校区、建筑、院系）
        loadData() // 再加载主数据
      }
    })

    return {
      activeTab,
      buildings,
      facilities,
      courses,
      teachers,
      events,
      loading,
      isAdmin,
      goToUserCenter,
      editDialogVisible,
      editType,
      editForm,
      addDialogVisible,
      addType,
      addForm,
      campuses,
      buildingsList,
      departments,
      handleDelete,
      handleEdit,
      confirmEdit,
      showAddDialog,
      confirmAdd,
      handleImportSuccess,
      handleImportError,
      getEntityName,
      formatDateTime,
      loadData
    }
  }
}
</script>

<style scoped>
.admin-view {
  max-width: 1400px;
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

:deep(.el-tabs__item) {
  font-size: 14px;
}

:deep(.el-button) {
  font-size: 13px;
}
</style>
