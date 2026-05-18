import React, { useMemo, useState } from 'react'
import { ArrowRight, Eye, EyeOff, LockKeyhole, Mail, Sparkles, UserRound } from 'lucide-react'
import './animated-auth.css'

function useEyeTransform() {
  const [eyeStyle, setEyeStyle] = useState({ '--eye-x': '0px', '--eye-y': '0px' })

  function onMouseMove(event) {
    const rect = event.currentTarget.getBoundingClientRect()
    const x = ((event.clientX - rect.left) / rect.width - 0.5) * 14
    const y = ((event.clientY - rect.top) / rect.height - 0.5) * 12
    setEyeStyle({ '--eye-x': `${x}px`, '--eye-y': `${y}px` })
  }

  function onMouseLeave() {
    setEyeStyle({ '--eye-x': '0px', '--eye-y': '0px' })
  }

  return { eyeStyle, onMouseMove, onMouseLeave }
}

function Character({ variant, mood }) {
  return (
    <div className={`auth-character ${variant} mood-${mood}`}>
      <div className="character-antenna left" />
      <div className="character-antenna right" />
      <div className="character-head">
        <div className="character-face">
          <span className="character-eye left" />
          <span className="character-eye right" />
          <span className="character-mouth" />
        </div>
      </div>
      <div className="character-body">
        <span>{variant === 'black' ? 'AI' : variant === 'yellow' ? '?' : '✓'}</span>
      </div>
      <div className="character-arm left" />
      <div className="character-arm right" />
    </div>
  )
}

function FloatingCharacters({ mode, focusField, loading, showPassword }) {
  const mood = loading ? 'loading' : showPassword ? 'peek' : focusField ? 'focus' : 'idle'
  return (
    <section className="characters-wrap">
      <div className="grid-glow" />
      <div className="character-shadow" />
      <Character variant="purple" mood={mood} />
      <Character variant="black" mood={mood} />
      <Character variant="orange" mood={mood} />
      <Character variant="yellow" mood={mood} />
      <div className="auth-chip chip-one">{mode === 'login' ? '继续学习' : '创建档案'}</div>
      <div className="auth-chip chip-two">AI诊断</div>
      <div className="auth-chip chip-three">智能题库</div>
    </section>
  )
}

function TextInput({ icon, label, type = 'text', value, onChange, onFocus, onBlur, placeholder, rightSlot }) {
  return (
    <label className="auth-field">
      <span>{label}</span>
      <div className="field-box">
        {icon}
        <input
          type={type}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          onFocus={onFocus}
          onBlur={onBlur}
          placeholder={placeholder}
        />
        {rightSlot}
      </div>
    </label>
  )
}

export default function AnimatedAuthPage({
  mode = 'login',
  onLogin,
  onRegister,
  onRegisterSubmit,
  onBackToLogin,
  loading = false,
  errorMessage = ''
}) {
  const { eyeStyle, onMouseMove, onMouseLeave } = useEyeTransform()
  const [focusField, setFocusField] = useState('')
  const [localError, setLocalError] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [form, setForm] = useState({
    username: '',
    password: '',
    realName: '',
    email: '',
    phone: ''
  })

  const copy = useMemo(() => {
    if (mode === 'register') {
      return {
        badge: 'Student Register',
        title: '创建你的学习账号',
        subtitle: '学生账号注册后即可进入题库训练，教师账号由数据库预置。',
        action: '注册学生账号',
        loadingText: '注册中...',
        switchText: '已经有账号？',
        switchAction: '返回登录'
      }
    }
    return {
      badge: 'Welcome Back',
      title: '欢迎回到 AgentEdu',
      subtitle: '登录后继续题库训练、查看评测结果和 AI 多智能体诊断。',
      action: '登录',
      loadingText: '登录中...',
      switchText: '还没有学生账号？',
      switchAction: '立即注册'
    }
  }, [mode])

  function update(key, value) {
    setForm((current) => ({ ...current, [key]: value }))
  }

  function validate() {
    if (!form.username.trim()) return '请输入用户名'
    if (!form.password) return '请输入密码'
    if (mode === 'register' && form.password.length < 6) return '密码至少 6 位'
    if (mode === 'register' && !form.realName.trim()) return '请输入真实姓名'
    return ''
  }

  function handleSubmit(event) {
    event.preventDefault()
    const message = validate()
    setLocalError(message)
    if (message || loading) return

    if (mode === 'register') {
      onRegisterSubmit?.({ ...form })
      return
    }
    onLogin?.(form.username.trim(), form.password)
  }

  function handleSwitch() {
    if (mode === 'register') {
      onBackToLogin?.()
      return
    }
    onRegister?.()
  }

  const visibleError = localError || errorMessage

  return (
    <main className="animated-auth-page" style={eyeStyle} onMouseMove={onMouseMove} onMouseLeave={onMouseLeave}>
      <div className="auth-shell-react">
        <section className="visual-panel">
          <div className="auth-brand-react">
            <div className="brand-mark-react">AI</div>
            <div>
              <strong>AgentEdu</strong>
              <span>多学科个性化学习平台</span>
            </div>
          </div>

          <FloatingCharacters mode={mode} focusField={focusField} loading={loading} showPassword={showPassword} />

          <div className="visual-copy">
            <p>AI Learning Assistant</p>
            <h2>{mode === 'login' ? '让每一次练习都有反馈' : '从第一道题开始建立学习画像'}</h2>
            <span>题库训练、在线评测、错因诊断、学习报告，在这里汇成一条清晰的成长路径。</span>
          </div>
        </section>

        <section className="form-panel">
          <div className="form-heading">
            <span><Sparkles size={14} /> {copy.badge}</span>
            <h1>{copy.title}</h1>
            <p>{copy.subtitle}</p>
          </div>

          <form className="react-auth-form" onSubmit={handleSubmit}>
            <TextInput
              label="用户名"
              value={form.username}
              onChange={(value) => update('username', value)}
              onFocus={() => setFocusField('username')}
              onBlur={() => setFocusField('')}
              placeholder="请输入用户名"
              icon={<UserRound size={18} />}
            />

            {mode === 'register' && (
              <>
                <TextInput
                  label="真实姓名"
                  value={form.realName}
                  onChange={(value) => update('realName', value)}
                  onFocus={() => setFocusField('realName')}
                  onBlur={() => setFocusField('')}
                  placeholder="请输入真实姓名"
                  icon={<UserRound size={18} />}
                />
                <div className="two-col">
                  <TextInput
                    label="邮箱"
                    value={form.email}
                    onChange={(value) => update('email', value)}
                    onFocus={() => setFocusField('email')}
                    onBlur={() => setFocusField('')}
                    placeholder="student@example.com"
                    icon={<Mail size={18} />}
                  />
                  <TextInput
                    label="手机号"
                    value={form.phone}
                    onChange={(value) => update('phone', value)}
                    onFocus={() => setFocusField('phone')}
                    onBlur={() => setFocusField('')}
                    placeholder="13800000000"
                    icon={<UserRound size={18} />}
                  />
                </div>
              </>
            )}

            <TextInput
              label="密码"
              type={showPassword ? 'text' : 'password'}
              value={form.password}
              onChange={(value) => update('password', value)}
              onFocus={() => setFocusField('password')}
              onBlur={() => setFocusField('')}
              placeholder={mode === 'register' ? '至少 6 位' : '请输入密码'}
              icon={<LockKeyhole size={18} />}
              rightSlot={
                <button className="peek-button" type="button" onClick={() => setShowPassword((value) => !value)}>
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              }
            />

            {visibleError && <div className="auth-error">{visibleError}</div>}

            <button className="submit-button" type="submit" disabled={loading}>
              {loading ? copy.loadingText : copy.action}
              <ArrowRight size={18} />
            </button>
          </form>

          <div className="auth-switch">
            <span>{copy.switchText}</span>
            <button type="button" onClick={handleSwitch}>{copy.switchAction}</button>
          </div>

          {mode === 'login' && (
            <div className="demo-accounts">
              <strong>演示账号</strong>
              <span>教师 teacher01 / 123456</span>
              <span>学生 student01 / 123456</span>
            </div>
          )}
        </section>
      </div>
    </main>
  )
}
