/**
* 图表界面
*/
<template>
  <div class="statistics-page">
    <el-breadcrumb separator-class="el-icon-arrow-right">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>数据可视化</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="statistics-filter">
      <el-radio-group v-model="query.scope" size="small" @change="handleScopeChange">
        <el-radio-button label="month">按月统计</el-radio-button>
        <el-radio-button label="year">按年统计</el-radio-button>
      </el-radio-group>

      <el-date-picker
        v-if="query.scope === 'month'"
        v-model="query.month"
        size="small"
        type="month"
        value-format="yyyy-MM"
        placeholder="选择月份">
      </el-date-picker>

      <el-date-picker
        v-else
        v-model="query.year"
        size="small"
        type="year"
        value-format="yyyy"
        placeholder="选择年份">
      </el-date-picker>

      <el-button size="small" type="primary" :loading="loading" @click="loadStatistics">查询</el-button>
      <span class="range-text" v-if="rangeText">统计范围：{{ rangeText }}</span>
    </div>

    <el-row :gutter="20" v-loading="loading" element-loading-text="统计数据加载中">
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <div class="chart-panel">
          <div class="canvas-box" ref="loginTopChart"></div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <div class="chart-panel">
          <div class="canvas-box" ref="stockOutTopChart"></div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <div class="chart-panel">
          <div class="canvas-box" ref="stockInChart"></div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <div class="chart-panel">
          <div class="canvas-box" ref="stockOutChart"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script type="text/ecmascript-6">
import Chart from 'echarts'
import { GoodsStatistics } from '@/api/basisMG'

export default {
  name: 'statistics',
  data() {
    const now = new Date()
    const year = String(now.getFullYear())
    const currentMonth = now.getMonth() + 1
    const month = currentMonth < 10 ? '0' + currentMonth : String(currentMonth)
    return {
      loading: false,
      rangeText: '',
      query: {
        scope: 'month',
        year: year,
        month: year + '-' + month
      },
      charts: {
        loginTop: null,
        stockOutTop: null,
        stockIn: null,
        stockOut: null
      },
      loginTopOption: {},
      stockOutTopOption: {},
      stockInOption: {},
      stockOutOption: {}
    }
  },
  created() {
    this.initOptions()
  },
  mounted() {
    this.initCharts()
    this.loadStatistics()
    window.addEventListener('resize', this.resizeCharts)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts)
    Object.keys(this.charts).forEach(key => {
      if (this.charts[key]) {
        this.charts[key].dispose()
      }
    })
  },
  methods: {
    initOptions() {
      this.loginTopOption = this.createPieOption('登录用户前五名', '登录次数')
      this.stockOutTopOption = this.createPieOption('出库物品前五名', '出库数量')
      this.stockInOption = this.createBarOption('入库数量展示', '入库数量', '#2f8fed')
      this.stockOutOption = this.createBarOption('出库数量展示', '出库数量', '#34a853')
    },
    initCharts() {
      this.charts.loginTop = Chart.init(this.$refs.loginTopChart)
      this.charts.stockOutTop = Chart.init(this.$refs.stockOutTopChart)
      this.charts.stockIn = Chart.init(this.$refs.stockInChart)
      this.charts.stockOut = Chart.init(this.$refs.stockOutChart)
      this.renderCharts()
    },
    createPieOption(title, seriesName) {
      return {
        color: ['#2f8fed', '#34a853', '#f6b23f', '#ef5350', '#8e63ce'],
        title: {
          text: title,
          left: 'center',
          top: 12,
          textStyle: {
            fontSize: 15,
            fontWeight: 'normal',
            color: '#303133'
          }
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a}<br/>{b}: {c} ({d}%)'
        },
        legend: {
          type: 'scroll',
          bottom: 8,
          left: 'center',
          itemWidth: 12,
          itemHeight: 12,
          textStyle: {
            color: '#606266'
          }
        },
        series: [{
          name: seriesName,
          type: 'pie',
          radius: '58%',
          center: ['50%', '52%'],
          data: [],
          label: {
            formatter: '{b}: {c}'
          }
        }]
      }
    },
    createBarOption(title, seriesName, color) {
      return {
        title: {
          text: title,
          left: 12,
          top: 12,
          textStyle: {
            fontSize: 15,
            fontWeight: 'normal',
            color: '#303133'
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: 55,
          right: 22,
          top: 58,
          bottom: 45
        },
        xAxis: {
          type: 'category',
          data: [],
          axisTick: {
            alignWithLabel: true
          },
          axisLabel: {
            color: '#606266'
          }
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            color: '#606266'
          }
        },
        series: [{
          name: seriesName,
          type: 'bar',
          barMaxWidth: 28,
          data: [],
          itemStyle: {
            color: color
          }
        }]
      }
    },
    loadStatistics() {
      const params = this.buildQueryParams()
      if (!params) {
        return
      }

      this.loading = true
      GoodsStatistics(params)
        .then(res => {
          this.loading = false
          if (!res.success) {
            this.$message.error(res.msg)
            return
          }

          const data = res.data || {}
          this.rangeText = (data.startDate || '') + ' 至 ' + (data.endDate || '')
          this.setPieData(this.loginTopOption, data.loginUserTop5)
          this.setPieData(this.stockOutTopOption, data.stockOutProductTop5)
          this.setBarData(this.stockInOption, data.stockInQuantity)
          this.setBarData(this.stockOutOption, data.stockOutQuantity)
          this.renderCharts()
        })
        .catch(() => {
          this.loading = false
          this.$message.error('统计数据加载失败，请稍后再试')
        })
    },
    buildQueryParams() {
      if (this.query.scope === 'year') {
        if (!this.query.year) {
          this.$message.warning('请选择统计年份')
          return null
        }
        return {
          scope: 'year',
          year: Number(this.query.year)
        }
      }

      if (!this.query.month) {
        this.$message.warning('请选择统计月份')
        return null
      }
      const parts = this.query.month.split('-')
      return {
        scope: 'month',
        year: Number(parts[0]),
        month: Number(parts[1])
      }
    },
    handleScopeChange() {
      this.loadStatistics()
    },
    setPieData(option, list) {
      option.series[0].data = this.normalizeChartItems(list)
    },
    setBarData(option, list) {
      const items = this.normalizeChartItems(list)
      option.xAxis.data = items.map(item => item.name)
      option.series[0].data = items.map(item => item.value)
    },
    normalizeChartItems(list) {
      return (list || []).map(item => {
        return {
          name: item.name,
          value: Number(item.value || 0)
        }
      })
    },
    renderCharts() {
      if (this.charts.loginTop) {
        this.charts.loginTop.setOption(this.loginTopOption, true)
      }
      if (this.charts.stockOutTop) {
        this.charts.stockOutTop.setOption(this.stockOutTopOption, true)
      }
      if (this.charts.stockIn) {
        this.charts.stockIn.setOption(this.stockInOption, true)
      }
      if (this.charts.stockOut) {
        this.charts.stockOut.setOption(this.stockOutOption, true)
      }
    },
    resizeCharts() {
      Object.keys(this.charts).forEach(key => {
        if (this.charts[key]) {
          this.charts[key].resize()
        }
      })
    }
  }
}
</script>

<style>
.statistics-page {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
}

.statistics-filter {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  min-height: 60px;
  margin-top: 18px;
  padding: 12px 16px;
  box-sizing: border-box;
  background-color: #fff;
  border-radius: 5px;
}

.range-text {
  color: #606266;
  font-size: 13px;
}

.chart-panel {
  height: 340px;
  margin-top: 20px;
  padding: 10px;
  box-sizing: border-box;
  background-color: #fff;
  border-radius: 5px;
}

.canvas-box {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
}
</style>
