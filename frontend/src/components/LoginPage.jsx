import { useEffect, useRef, useState } from 'react';
import { Eye, EyeOff, ArrowRight, ShieldAlert } from 'lucide-react';
import { DashboardApi } from '../services/api';
import './LoginPage.css';

const IP_PRESETS = [
  { label: 'Việt Nam', ip: '118.70.128.1' },
  { label: 'Nhật Bản', ip: '220.100.0.1' },
  { label: 'Hoa Kỳ', ip: '128.122.1.1' }
];

export default function LoginPage({ onLoginSuccess }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [email, setEmail] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [ipAddress, setIpAddress] = useState(IP_PRESETS[0].ip);
  const [customIp, setCustomIp] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [registering, setRegistering] = useState(false);
  const blockedDialog = useRef(null);
  const [challenge, setChallenge] = useState(null);
  const [code, setCode] = useState('');
  const [otpMessage, setOtpMessage] = useState('');
  const [otpBusy, setOtpBusy] = useState(false);
  const [now, setNow] = useState(() => Date.now());

  useEffect(() => {
    if (!challenge) return;
    const timer = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(timer);
  }, [challenge]);

  useEffect(() => {
    if (result?.type === 'blocked' || challenge) {
      if (!blockedDialog.current.open) blockedDialog.current.showModal();
    } else {
      blockedDialog.current.close();
    }
  }, [result, challenge]);

  function openChallenge(value) {
    setChallenge(value);
    setCode('');
    setOtpMessage('');
    setNow(Date.now());
  }

  async function verifyOtp(event) {
    event.preventDefault();
    if (otpBusy) return;
    setOtpBusy(true);
    setOtpMessage('');
    try {
      const response = await DashboardApi.verifyOtp(challenge.challengeId, code);
      if (!response.success) throw response;
      setChallenge(null);
      if (response.purpose === 'EMAIL_VERIFY') {
        setRegistering(false);
        setResult({ type: 'success', title: 'Đã xác minh email', message: response.message });
      } else {
        onLoginSuccess(response);
      }
    } catch (error) {
      setOtpMessage(error.message || 'Không thể xác minh mã. Vui lòng thử lại.');
    } finally {
      setOtpBusy(false);
    }
  }

  async function resendOtp() {
    if (otpBusy) return;
    setOtpBusy(true);
    try {
      const response = await DashboardApi.resendOtp(challenge.challengeId);
      openChallenge(response);
      setOtpMessage('Đã gửi mã mới. Mã cũ không còn hiệu lực.');
    } catch (error) {
      setOtpMessage(error.message || 'Không thể gửi lại mã.');
    } finally { setOtpBusy(false); }
  }

  async function dismissPopup() {
    if (otpBusy) return;
    if (challenge) {
      setOtpBusy(true);
      try {
        await DashboardApi.cancelOtp(challenge.challengeId);
        setChallenge(null);
      } catch (error) {
        setOtpMessage(error.message || 'Không thể hủy yêu cầu. Vui lòng thử lại.');
        return;
      } finally { setOtpBusy(false); }
    }
    setResult(null);
  }

  async function handleSubmit(event) {
    event.preventDefault();
    if (loading) return;
    setLoading(true);
    setResult(null);
    try {
      if (registering) {
        const response = await DashboardApi.register(username.trim(), password, username.trim(), email.trim());
        if (!response.success) throw response;
        openChallenge(response.verification);
      } else {
        const response = await DashboardApi.login(username.trim(), password, (ipAddress === 'custom' ? customIp : ipAddress).trim());
        if (response.verificationRequired) { openChallenge(response); return; }
        if (!response.success) throw response;
        onLoginSuccess(response);
      }
    } catch (error) {
      const blocked = Boolean(error.detectionDetails);
      setResult({ type: blocked ? 'blocked' : 'error', title: blocked ? 'Đăng nhập bị chặn' : registering ? 'Không thể tạo tài khoản' : 'Đăng nhập thất bại', message: error.message || 'Không thể kết nối đến máy chủ. Vui lòng thử lại.', details: error.detectionDetails });
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="login-page">
      <section className="login-card" aria-labelledby="login-title">
        <h1 id="login-title">{registering ? 'Tạo tài khoản' : 'Đăng nhập'}</h1>
        <p className="login-description">{registering ? 'Tạo tài khoản để bắt đầu.' : 'Truy cập tài khoản đăng nhập.'}</p>
        <form id="login-form" onSubmit={handleSubmit}>
          <fieldset disabled={loading} className="login-fields">
            <label htmlFor="login-username">Tên đăng nhập</label>
            <input id="login-username" name="username" autoComplete="username" required pattern=".*\S.*" maxLength={100} value={username} onChange={(event) => setUsername(event.target.value)} placeholder="Nhập tên đăng nhập" />
            {registering && <>
              <label htmlFor="register-email">Email</label>
              <input id="register-email" name="email" type="email" autoComplete="email" required maxLength={254} value={email} onChange={(event) => setEmail(event.target.value)} placeholder="Nhập email nhận mã xác minh" />
            </>}
            <label htmlFor="login-password">Mật khẩu</label>
            <div className="login-password">
              <input id="login-password" name="password" type={showPassword ? 'text' : 'password'} autoComplete={registering ? 'new-password' : 'current-password'} required value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Nhập mật khẩu" />
              <button type="button" className="login-eye" onClick={() => setShowPassword(!showPassword)} aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'} aria-pressed={showPassword}>{showPassword ? <EyeOff size={18} /> : <Eye size={18} />}</button>
            </div>
            <button className="login-submit" type="submit">{loading ? 'Đang xử lý…' : registering ? 'Tạo tài khoản' : 'Đăng nhập'}<ArrowRight size={17} aria-hidden="true" /></button>
          </fieldset>
        </form>
        <button className="login-switch" disabled={loading} onClick={() => { setRegistering(!registering); setResult(null); }}>{registering ? 'Đã có tài khoản? Đăng nhập' : 'Chưa có tài khoản? Tạo tài khoản thử nghiệm'}</button>
        <div aria-live="polite" aria-atomic="true">
          {result && result.type !== 'blocked' && <section className={`login-result login-result-${result.type}`}>
            <h2>{result.title}</h2>
            <p>{result.message}</p>
          </section>}
        </div>
      </section>
      <dialog ref={blockedDialog} className="login-blocked-dialog" role="alertdialog" aria-labelledby="blocked-title" aria-describedby="blocked-description" onCancel={(event) => { event.preventDefault(); dismissPopup(); }}>
        <ShieldAlert size={44} aria-hidden="true" />
        <h2 id="blocked-title">{challenge?.purpose === 'EMAIL_VERIFY' ? 'Xác minh email của bạn' : 'Phát hiện đăng nhập bất thường'}</h2>
        <p id="blocked-description">{challenge ? challenge.message : result?.message || 'Lần đăng nhập này đã bị chặn để bảo vệ tài khoản của bạn.'}</p>
        {challenge ? <>
          <p>Mã đã gửi đến <strong>{challenge.maskedEmail}</strong>.</p>
          <form className="login-fields" onSubmit={verifyOtp}>
            <label htmlFor="email-otp">Mã xác minh gồm 6 chữ số</label>
            <input id="email-otp" name="otp" inputMode="numeric" autoComplete="one-time-code" pattern="[0-9]{6}" maxLength={6} required value={code} disabled={otpBusy} onChange={(event) => setCode(event.target.value.replace(/[^0-9]/g, ''))} />
            <p className="otp-expiry">{now >= Date.parse(challenge.expiresAt) ? 'Mã đã hết hạn. Hủy và thực hiện lại yêu cầu.' : `Mã còn hiệu lực ${Math.ceil((Date.parse(challenge.expiresAt) - now) / 1000)} giây.`}</p>
            <button className="login-submit" type="submit" disabled={otpBusy || now >= Date.parse(challenge.expiresAt)}>{otpBusy ? 'Đang xử lý…' : 'Xác minh'}</button>
          </form>
          <p className="otp-feedback" role="status">{otpMessage}</p>
          <div className="otp-actions">
            <button type="button" disabled={otpBusy || now < Date.parse(challenge.resendAvailableAt) || now >= Date.parse(challenge.expiresAt)} onClick={resendOtp}>{now < Date.parse(challenge.resendAvailableAt) ? `Gửi lại sau ${Math.ceil((Date.parse(challenge.resendAvailableAt) - now) / 1000)}s` : 'Gửi lại mã'}</button>
            <button type="button" onClick={dismissPopup} disabled={otpBusy}>Hủy</button>
          </div>
        </> : <button type="button" className="login-submit" onClick={dismissPopup}>Đã hiểu</button>}
      </dialog>
      {!registering && <aside className="login-simulation" aria-label="Cấu hình IP mô phỏng">
        <label htmlFor="login-ip">Vị trí đăng nhập</label>
        <select id="login-ip" value={ipAddress} disabled={loading} onChange={(event) => setIpAddress(event.target.value)}>
          {IP_PRESETS.map((preset) => <option key={preset.ip} value={preset.ip}>{preset.label} ({preset.ip})</option>)}
          <option value="custom">IP tùy chỉnh</option>
        </select>
        {ipAddress === 'custom' && <div className="login-custom-ip">
          <label htmlFor="custom-ip">Địa chỉ IP tùy chỉnh</label>
          <input id="custom-ip" name="ipAddress" form="login-form" type="text" required pattern=".*\S.*" maxLength={45} value={customIp} disabled={loading} onChange={(event) => setCustomIp(event.target.value)} placeholder="Ví dụ: 8.8.8.8" autoComplete="off" spellCheck={false} />
        </div>}
      </aside>}
    </main>
  );
}
