// Tab切换功能
function switchTab(tabName) {
    // 移除所有tab按钮的active类
    const tabButtons = document.querySelectorAll('.tab-button');
    tabButtons.forEach(button => button.classList.remove('active'));

    // 隐藏所有tab内容
    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(content => content.classList.remove('active'));

    // 激活当前tab按钮和内容
    event.target.classList.add('active');
    document.getElementById(tabName + 'Tab').classList.add('active');

    // 清空结果显示
    hideResult('convertResult');
    hideResult('revertResult');
}

// 显示加载状态
function showLoading(elementId) {
    document.getElementById(elementId).classList.add('show');
}

// 隐藏加载状态
function hideLoading(elementId) {
    document.getElementById(elementId).classList.remove('show');
}

// 显示结果
function showResult(elementId) {
    var element = document.getElementById(elementId);
    element.style.display = 'block';
    element.classList.add('show');
}

// 隐藏结果
function hideResult(elementId) {
    document.getElementById(elementId).classList.remove('show');
    document.getElementById(elementId).style.display = 'none';
}

// 显示错误提示
function showError(message) {
    // 创建错误提示元素
    var errorDiv = document.createElement('div');
    errorDiv.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #ff4757;
            color: white;
            padding: 12px 20px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(255, 71, 87, 0.3);
            z-index: 1000;
            animation: slideInRight 0.3s ease;
        `;
    errorDiv.textContent = message;

    // 添加动画样式
    var style = document.createElement('style');
    style.textContent = `
            @keyframes slideInRight {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
        `;
    document.head.appendChild(style);

    document.body.appendChild(errorDiv);

    // 3秒后自动移除
    setTimeout(function() {
        if (errorDiv.parentNode) {
            errorDiv.parentNode.removeChild(errorDiv);
        }
    }, 3000);
}

// 检查是否启用密码验证
function checkPasswordEnabled() {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', '/password-enabled', true);
    xhr.onload = function() {
        if (this.status === 200) {
            try {
                var res = JSON.parse(this.response);
                if (res.code === 200 && res.data === true) {
                    // 启用了密码验证，显示密码输入框
                    setTimeout(function() {
                        document.getElementById('passwordContainer').classList.add('show');
                        // 加载本地缓存的密码
                        loadSavedPassword();
                    }, 300);
                }
            } catch (e) {
                console.error('检查密码配置失败', e);
            }
        }
    };
    xhr.send();
}

// 保存密码到本地缓存
function savePassword(password) {
    try {
        localStorage.setItem('shortcut_password', password);
    } catch (e) {
        console.warn('无法保存密码到本地缓存:', e);
    }
}

// 从本地缓存加载密码
function loadSavedPassword() {
    try {
        var savedPassword = localStorage.getItem('shortcut_password');
        if (savedPassword) {
            document.getElementById('password').value = savedPassword;
        }
    } catch (e) {
        console.warn('无法从本地缓存加载密码:', e);
    }
}

// 清除本地缓存的密码
function clearSavedPassword() {
    try {
        localStorage.removeItem('shortcut_password');
    } catch (e) {
        console.warn('无法清除本地缓存的密码:', e);
    }
}

// URL转换为短地址
function convert() {
    var urlInput = document.getElementById("url");
    var url = urlInput.value.trim();

    if (!url) {
        showError('请输入要转换的网址');
        urlInput.focus();
        return;
    }

    // 简单的URL格式验证
    if (!url.startsWith('http://') && !url.startsWith('https://')) {
        showError('请输入有效的网址（需要包含 http:// 或 https://）');
        urlInput.focus();
        return;
    }

    hideResult('convertResult');
    showLoading('convertLoading');

    var xhr = new XMLHttpRequest();
    // Base64编码URL
    var encodedUrl = btoa(encodeURIComponent(url));

    // 准备JSON请求数据
    var requestData = {
        url: encodedUrl
    };

    // 如果密码输入框可见，则添加密码参数
    var passwordContainer = document.getElementById('passwordContainer');
    if (passwordContainer.classList.contains('show')) {
        var password = document.getElementById('password').value.trim();
        if (password) {
            requestData.password = password;
        }
    }

    xhr.open('post', '/convert', true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    xhr.onload = function () {
        hideLoading('convertLoading');

        if (this.status === 200) {
            try {
                var res = JSON.parse(this.response);

                if (res.code !== 200) {
                    showError(res.message || '转换失败，请重试');
                    // 如果是密码错误，清除本地缓存的密码
                    if (res.message && (res.message.includes('密码') || res.message.includes('password'))) {
                        clearSavedPassword();
                    }
                    return;
                }

                // 转换成功，保存密码到本地缓存
                var passwordContainer = document.getElementById('passwordContainer');
                if (passwordContainer.classList.contains('show')) {
                    var password = document.getElementById('password').value.trim();
                    if (password) {
                        savePassword(password);
                    }
                }

                // 构建短地址URL
                var shortUrl = document.getElementById("preurl").innerText + "/" + res.data;

                // 更新结果显示
                var convertRepo = document.getElementById("convertRepo");
                var resultUrl = convertRepo.querySelector('.result-url');
                convertRepo.setAttribute("href", shortUrl);
                resultUrl.textContent = shortUrl;

                // 更新二维码
                var convertImage = document.getElementById("convertImage");
                convertImage.setAttribute("src", "/qrcode?url=" + encodeURIComponent(shortUrl));

                // 显示结果
                showResult('convertResult');

            } catch (e) {
                showError('服务器响应格式错误');
            }
        } else {
            showError('网络请求失败，请检查网络连接');
        }
    };

    xhr.onerror = function() {
        hideLoading('convertLoading');
        showError('网络连接失败，请重试');
    };

    xhr.send(JSON.stringify(requestData));
}

// 短地址还原
function revert() {
    var shortUrlInput = document.getElementById("shortUrl");
    var shortUrl = shortUrlInput.value.trim();

    if (!shortUrl) {
        showError('请输入要还原的短网址');
        shortUrlInput.focus();
        return;
    }

    hideResult('revertResult');
    showLoading('revertLoading');

    var xhr = new XMLHttpRequest();
    xhr.open('post', '/revert', true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    xhr.onload = function () {
        hideLoading('revertLoading');

        if (this.status === 200) {
            try {
                var res = JSON.parse(this.response);

                if (res.code !== 200) {
                    showError(res.message || '还原失败，请检查短网址是否正确');
                    return;
                }

                if (!res.data) {
                    showError('未找到对应的原始网址');
                    return;
                }

                // 更新结果显示
                var revertRepo = document.getElementById("revertRepo");
                var resultUrl = revertRepo.querySelector('.result-url');
                revertRepo.setAttribute("href", res.data);
                resultUrl.textContent = res.data;

                // 更新二维码
                var revertImage = document.getElementById("revertImage");
                revertImage.setAttribute("src", "/qrcode?url=" + encodeURIComponent(res.data));

                // 显示结果
                showResult('revertResult');

            } catch (e) {
                showError('服务器响应格式错误');
            }
        } else {
            showError('网络请求失败，请检查网络连接');
        }
    };

    xhr.onerror = function() {
        hideLoading('revertLoading');
        showError('网络连接失败，请重试');
    };

    xhr.send(JSON.stringify({shortUrl: shortUrl}));
}


// 页面加载完成后检查密码配置
document.addEventListener('DOMContentLoaded', function() {
    checkPasswordEnabled();
});

// 添加回车键支持
document.getElementById('url').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        convert();
    }
});

document.getElementById('shortUrl').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        revert();
    }
});

// 为密码输入框添加回车键支持
document.addEventListener('keypress', function(e) {
    if (e.target.id === 'password' && e.key === 'Enter') {
        convert();
    }
});

// 复制结果链接
function copyResult(elementId) {
    var element = document.getElementById(elementId);
    var resultUrl = element.querySelector('.result-url');
    var text = resultUrl.textContent;

    if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(function() {
            showSuccess('已复制到剪贴板');
        });
    } else {
        // 兼容旧浏览器
        var textArea = document.createElement("textarea");
        textArea.value = text;
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
        showSuccess('已复制到剪贴板');
    }
}

// 添加复制功能
function copyToClipboard(text) {
    if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(function() {
            showSuccess('已复制到剪贴板');
        });
    } else {
        // 兼旧浏览器
        var textArea = document.createElement("textarea");
        textArea.value = text;
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
        showSuccess('已复制到剪贴板');
    }
}

// 显示成功提示
function showSuccess(message) {
    var successDiv = document.createElement('div');
    successDiv.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #2ed573;
            color: white;
            padding: 12px 20px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(46, 213, 115, 0.3);
            z-index: 1000;
            animation: slideInRight 0.3s ease;
        `;
    successDiv.textContent = message;
    document.body.appendChild(successDiv);

    setTimeout(function() {
        if (successDiv.parentNode) {
            successDiv.parentNode.removeChild(successDiv);
        }
    }, 2000);
}

// 为结果链接添加点击复制功能
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('result-link')) {
        e.preventDefault();
        copyToClipboard(e.target.textContent);

        // 如果是链接地址，也可以选择打开链接
        setTimeout(function() {
            if (confirm('是否要在新窗口中打开这个链接？')) {
                window.open(e.target.href, '_blank');
            }
        }, 500);
    }
});
