import axios from "axios";
import {ElMessage} from "element-plus";

const authItemName = 'access_token'


const defaultFailure = (message, code, url) => {
    console.warn(`请求地址: ${url}, 状态码: ${code},错误信息: ${message}`);
    ElMessage.warning(message);
}

const defaultError = (error) => {
    console.error(error)
    const status = error.response.status
    if (status === 429) {
        ElMessage.error(error.response.data.message)
    } else {
        ElMessage.error('发生了一些错误，请联系管理员')
    }
}

function storeAccessToken(token, remember, expire) {
    const authObj = {token: token, expire: expire};
    const str = JSON.stringify(authObj);
    if (remember) {
        localStorage.setItem(authItemName, str);
    }else {
        sessionStorage.setItem(authItemName, str)
    }
}

function takeAccessToken() {
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName);
    if (!str) return null;
    const  authObj = JSON.parse(str)
    if (authObj.expire <= new Date()) {
        deleteAccessToken()
        ElMessage.warning('登录状态已过期,请重新登录')
        return null
    }
    return authObj.token
}

function deleteAccessToken() {
    localStorage.removeItem(authItemName);
    sessionStorage.removeItem(authItemName);
}

function accessHeader() {
    const token = takeAccessToken();
    return token ? {'Authorization': `Bearer ${takeAccessToken()}`} : {}
}

function internalPost(url, data, headers, success, failure, error = defaultError) {
    axios.post(url, data, {headers: headers}).then(({data}) => {//三个data
        if (data.code === 200) {
            success(data.data);
        } else {
            failure(data.message, data.code, url)
        }
    }).catch((err) => {
        error(err)
    })
}

function internalGet(url, headers, success, failure, error = defaultError) {
    axios.get(url, {headers: headers}).then(({data}) => {//三个data
        if (data.code === 200) {
            success(data.data);
        } else {
            failure(data.message, data.code, url)
        }
    }).catch((err) => {
        error(err)
    })
}

function get(url, success, failure = defaultFailure) {
    internalGet(url, accessHeader(), success, failure)
}

function post(url, data, success, failure = defaultFailure) {
    internalPost(url, data, accessHeader(), success, failure)
}

function login(username, password, remember, success, failure = defaultFailure) {
    internalPost(
        'api/auth/login',
        {
            username: username,
            password: password,
        },
        {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        (data) => {
            storeAccessToken(data.token, remember, data.expire);
            ElMessage.success(`登录成功,欢迎${data.username}进入系统`)
            success(data);
        },
        failure
    )
}

function logout(success, failure = defaultFailure) {
    get('api/auth/logout', () => {
        deleteAccessToken()
        ElMessage.success('退出登录成功')
        success();
    }, failure)
}

function unauthorized(){
    return !takeAccessToken()
}

export {login, logout, get, post, unauthorized}