import axios from 'axios'
import { req } from './axiosFun'

/**
 * 商品/库存管理
 **/
export const GoodsList = (params) => req('post', '/api/Goods/list', params)
export const GoodsSave = (params) => req('post', '/api/Goods/save', params)
export const GoodsDelete = (params) => axios.delete('/api/Goods/delete?ids=' + params + '&token=' + localStorage.getItem('logintoken')).then(res => res.data)
export const GoodsAlarmSave = (params) => req('post', '/api/Goods/alarm', params)
export const GoodsStockIn = (params) => req('post', '/api/Goods/stockIn', params)
export const GoodsStockOut = (params) => req('post', '/api/Goods/stockOut', params)
export const GoodsStatistics = (params) => {
  return axios.get('/api/Goods/statistics', {
    params: params,
    headers: {
      token: localStorage.getItem('logintoken')
    }
  }).then(res => res.data)
}
export const GoodsImgUpload = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return axios.post('/api/Goods/img/upload', formData, {
    headers: {
      token: localStorage.getItem('logintoken'),
      'Content-Type': 'multipart/form-data'
    }
  }).then(res => res.data)
}
export const GoodsImgList = (productId) => {
  return axios.get('/api/Goods/img/list', {
    params: { productId },
    headers: {
      token: localStorage.getItem('logintoken')
    }
  }).then(res => res.data)
}
export const GoodsFind = (keyword) => {
  return axios.get('/api/Goods/find', {
    params: { keyword },
    headers: {
      token: localStorage.getItem('logintoken')
    }
  }).then(res => res.data)
}
export const GoodsBarcodeUrl = (id, code) => {
  return '/api/Goods/barcode?id=' + encodeURIComponent(id) +
    '&code=' + encodeURIComponent(code) +
    '&token=' + encodeURIComponent(localStorage.getItem('logintoken') || '')
}
export const GoodsImport = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return axios.post('/api/Goods/import', formData, {
    headers: {
      token: localStorage.getItem('logintoken'),
      'Content-Type': 'multipart/form-data'
    }
  }).then(res => res.data)
}
export const GoodsExport = (params) => {
  return axios({
    method: 'get',
    url: '/api/Goods/export',
    params: params,
    headers: {
      token: localStorage.getItem('logintoken')
    },
    responseType: 'blob'
  })
}

/**
 * 机器信息管理
 **/
export const MachineList = (params) => req('post', '/api/Machine/list', params)
export const MachineSave = (params) => req('post', '/api/Machine/save', params)
export const MachineDelete = (params) => axios.delete('/api/Machine/delete?ids=' + params + '&token=' + localStorage.getItem('logintoken')).then(res => res.data)

/**
 * 货道信息管理
 **/
export const MachineAisleList = (params) => req('post', '/api/MachineAisle/list', params)
export const MachineAisleDelete = (params) => axios.delete('/api/MachineAisle/delete?ids=' + params + '&token=' + localStorage.getItem('logintoken')).then(res => res.data)
export const MachineAisleRsave = (params) => req('post', '/api/MachineAisle/save', params)
