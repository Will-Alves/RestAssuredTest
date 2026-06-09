try {
  (function applyBranding() {
    var c = document.createElement('canvas');
    c.width = 64; c.height = 64;
    var ctx = c.getContext('2d');
    ctx.fillStyle = '#1a73e8';
    ctx.beginPath();
    if (ctx.roundRect) ctx.roundRect(0, 0, 64, 64, 12); else ctx.rect(0, 0, 64, 64);
    ctx.fill();
    ctx.fillStyle = '#fff';
    ctx.font = 'bold 28px Arial';
    ctx.textAlign = 'center';
    ctx.fillText('RA', 32, 40);
    document.querySelectorAll("link[rel*='icon']").forEach(function (e) { e.parentNode.removeChild(e); });
    var f = document.createElement('link');
    f.rel = 'icon';
    f.href = c.toDataURL('image/png');
    document.head.appendChild(f);
    var logo = document.querySelector('.nav-logo .logo');
    if (logo) {
      logo.innerHTML = '';
      logo.style.cssText = 'background:none !important;width:40px;height:40px;display:flex;align-items:center;justify-content:center;';
      var i = document.createElement('i');
      i.className = 'fa fa-flask';
      i.style.cssText = 'font-size:30px;color:#1a73e8;';
      logo.appendChild(i);
    }
  })();
} catch (e) {}

try {
  setTimeout(function () {
    var c = document.createElement('canvas');
    c.width = 64; c.height = 64;
    var ctx = c.getContext('2d');
    ctx.fillStyle = '#1a73e8';
    ctx.beginPath();
    if (ctx.roundRect) ctx.roundRect(0, 0, 64, 64, 12); else ctx.rect(0, 0, 64, 64);
    ctx.fill();
    ctx.fillStyle = '#fff';
    ctx.font = '38px FontAwesome';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('', 32, 34);
    document.querySelectorAll("link[rel*='icon']").forEach(function (e) { e.parentNode.removeChild(e); });
    var f = document.createElement('link');
    f.rel = 'icon';
    f.href = c.toDataURL('image/png');
    document.head.appendChild(f);
  }, 1200);
} catch (e) {}
