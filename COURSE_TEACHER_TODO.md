# 课程-教师关联管理功能 - 待完成代码

## 需要在 AdminView.vue 中添加的剩余代码

### 1. 在 handleDelete 函数后添加 handleDeleteCourseTeacher 函数

```javascript
const handleDeleteCourseTeacher = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除课程 "${row.course?.name || row.courseId}" 和教师 "${row.teacher?.name || row.teacherId}" 的关联吗？`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await courseTeacherAPI.deleteCourseTeacher(
      row.courseId,
      row.teacherId,
      row.semester
    )
    
    ElMessage.success('删除成功')
    loadCourseTeachers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      const errorMsg = error.response?.data?.message || error.message || '删除失败，请稍后重试'
      ElMessage.error(errorMsg)
    }
  }
}
```

### 2. 在 confirmEdit 函数中的 switch 语句添加 courseTeacher 分支

在 `switch(editType.value)` 的两个地方都需要添加：

**第一处 - 构建更新数据：**
```javascript
case 'courseTeacher':
  updatedData = {
    courseId: editForm.value.courseId,
    teacherId: editForm.value.teacherId,
    semester: editForm.value.semester,
    role: editForm.value.role || '主讲',
    remarks: editForm.value.remarks || ''
  }
  break
```

**第二处 - 调用API：**
```javascript
case 'courseTeacher':
  // 获取原始的主键信息（因为主键包含semester，可能会改变）
  const originalSemester = editRowId.value.semester
  await courseTeacherAPI.updateCourseTeacher(
    editForm.value.courseId,
    editForm.value.teacherId,
    originalSemester,
    updatedData
  )
  break
```

### 3. 在 confirmEdit 开始处添加验证修改

```javascript
const confirmEdit = async () => {
  // 对于courseTeacher类型，不需要检查name字段
  if (editType.value !== 'courseTeacher') {
    if (!editForm.value.name || editForm.value.name.trim() === '') {
      ElMessage.warning('名称不能为空')
      return
    }
  } else {
    // courseTeacher的特殊验证
    if (!editForm.value.semester || editForm.value.semester.trim() === '') {
      ElMessage.warning('学期不能为空')
      return
    }
  }
  // ... 其余代码
}
```

### 4. 在 confirmAdd 函数中添加 courseTeacher 分支

```javascript
case 'courseTeacher':
  if (!addForm.value.courseId) {
    ElMessage.warning('请选择课程')
    return
  }
  if (!addForm.value.teacherId) {
    ElMessage.warning('请选择教师')
    return
  }
  if (!addForm.value.semester || addForm.value.semester.trim() === '') {
    ElMessage.warning('学期不能为空')
    return
  }
  
  newData = {
    courseId: addForm.value.courseId,
    teacherId: addForm.value.teacherId,
    semester: addForm.value.semester,
    role: addForm.value.role || '主讲',
    remarks: addForm.value.remarks || ''
  }
  await courseTeacherAPI.createCourseTeacher(newData)
  break
```

### 5. 在 getEntityName 函数中添加 courseTeacher

```javascript
const getEntityName = (type) => {
  const names = {
    building: '建筑',
    facility: '设施',
    course: '课程',
    teacher: '教师',
    event: '活动',
    courseTeacher: '课程-教师关联'
  }
  return names[type] || type
}
```

### 6. 在 getRowId 函数中添加 courseTeacher

```javascript
const getRowId = (type, row) => {
  switch(type) {
    case 'building': return row.buildingId
    case 'facility': return row.facilityId
    case 'course': return row.courseId
    case 'teacher': return row.teacherId
    case 'event': return row.eventId
    case 'courseTeacher': 
      return {
        courseId: row.courseId,
        teacherId: row.teacherId,
        semester: row.semester
      }
    default: return null
  }
}
```

### 7. 在 handleEdit 函数中添加 courseTeacher 处理

```javascript
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
  // courseTeacher 不需要特殊处理，直接使用 courseId 和 teacherId
  
  console.log('编辑表单数据:', editForm.value)
  editRowId.value = getRowId(type, row)
  editDialogVisible.value = true
}
```

### 8. 在 onMounted 中加载 courseTeachers 数据

```javascript
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
    loadCourseTeachers() // 加载课程-教师关联数据
  }
})
```

### 9. 在 return 语句中添加导出

```javascript
return {
  activeTab,
  buildings,
  facilities,
  courses,
  teachers,
  events,
  courseTeachers,  // 新增
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
  handleDeleteCourseTeacher,  // 新增
  handleEdit,
  confirmEdit,
  showAddDialog,
  confirmAdd,
  handleImportSuccess,
  handleImportError,
  getEntityName,
  formatDateTime,
  loadData,
  loadCourseTeachers  // 新增
}
```

## 测试步骤

1. 重启后端服务
2. 访问管理后台
3. 切换到"🔗 课程-教师关联"标签页
4. 测试以下功能：
   - 查看所有关联记录
   - 添加新的关联（选择课程、教师、输入学期）
   - 编辑现有关联（修改学期、角色、备注）
   - 删除关联
   - 刷新数据

## 注意事项

1. CourseTeacher 使用复合主键 (courseId, teacherId, semester)
2. 编辑时课程和教师字段被禁用，只能修改学期、角色和备注
3. 如果学期改变，实际上是删除旧记录并创建新记录
4. 确保后端返回的数据中包含 course 和 teacher 的完整对象信息
