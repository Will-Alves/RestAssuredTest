try {
  (function applyBranding() {
    var c = document.createElement('canvas');
    c.width = 64; c.height = 64;
    var ctx = c.getContext('2d');
    ctx.fillStyle = '#0d1117';
    ctx.beginPath();
    if (ctx.roundRect) ctx.roundRect(0, 0, 64, 64, 12); else ctx.rect(0, 0, 64, 64);
    ctx.fill();
    ctx.strokeStyle = '#39d0c4';
    ctx.lineWidth = 3;
    ctx.beginPath();
    if (ctx.roundRect) ctx.roundRect(2, 2, 60, 60, 10); else ctx.rect(2, 2, 60, 60);
    ctx.stroke();
    ctx.fillStyle = '#39d0c4';
    ctx.font = 'bold 26px Arial';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('RA', 32, 34);
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
      i.style.cssText = 'font-size:28px;color:#39d0c4;';
      logo.appendChild(i);
    }
  })();
} catch (e) {}
