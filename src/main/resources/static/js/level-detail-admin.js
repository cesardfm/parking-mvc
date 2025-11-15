document.addEventListener('DOMContentLoaded', () => {
  const adminPanel = document.getElementById('adminPanel');
  const panelSiteId = document.getElementById('panelSiteId');
  const panelSitePos = document.getElementById('panelSitePos');
  const panelVehicle = document.getElementById('panelVehicleType');
  const panelSave = document.getElementById('panelSave');
  const panelCancel = document.getElementById('panelCancel');

  let currentElement = null;
  let currentSiteId = null;

  function openPanelFor(element) {
    const status = element.getAttribute('data-status');
    if (status === 'disabled') {
      alert('Este espacio está deshabilitado y no se puede modificar.');
      return;
    }

    currentElement = element;
    currentSiteId = element.getAttribute('data-site-id');

    panelSiteId.textContent = currentSiteId;
    panelSitePos.textContent = element.getAttribute('data-row') + '-' + element.getAttribute('data-col');

    // set vehicle
    const vehicle = element.getAttribute('data-vehicle') || 'none';
    panelVehicle.value = vehicle;

    // set status radios
    const statusVal = status || 'available';
    const radios = document.getElementsByName('panelStatus');
    radios.forEach(r => r.checked = (r.value === statusVal));

    adminPanel.classList.remove('hidden');
  }

  function closePanel() {
    adminPanel.classList.add('hidden');
    currentElement = null;
    currentSiteId = null;
  }

  // Click en cada sitio
  document.querySelectorAll('.site-box').forEach(box => {
    box.addEventListener('click', (e) => {
      openPanelFor(box);
    });
  });

  panelCancel.addEventListener('click', (e) => {
    e.preventDefault();
    closePanel();
  });

  panelSave.addEventListener('click', (e) => {
    e.preventDefault();
    if (!currentElement || !currentSiteId) return alert('No hay sitio seleccionado');

    // Obtener valores
    const newVehicle = panelVehicle.value;
    const radios = document.getElementsByName('panelStatus');
    let newStatus = null;
    radios.forEach(r => { if (r.checked) newStatus = r.value; });

    if (!newStatus) return alert('Selecciona un estado');

    // Preparar payload
    const payload = {
      siteId: currentSiteId,
      status: newStatus,
      vehicleType: newVehicle
    };

    // Llamada al servidor (endpoint necesario: /sites/update)
    fetch('/sites/update', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
      .then(res => {
        if (!res.ok) throw new Error('Error en servidor');
        return res.json();
      })
      .then(data => {
        if (data.success) {
          // Actualizar visual
          currentElement.setAttribute('data-status', newStatus);
          currentElement.setAttribute('data-vehicle', newVehicle);

          // actualizar clases visuales
          currentElement.classList.remove('bg-success-500','border-success-600','bg-danger-500','border-danger-600','bg-warning-500','border-warning-600','bg-gray-400','border-gray-500');
          if (newStatus === 'available') {
            currentElement.classList.add('bg-success-500','border-success-600');
          } else if (newStatus === 'occupied') {
            currentElement.classList.add('bg-danger-500','border-danger-600');
          } else if (newStatus === 'reserved') {
            currentElement.classList.add('bg-warning-500','border-warning-600');
          }

          alert('Sitio actualizado');
          closePanel();
        } else {
          alert('Error: ' + (data.message || 'no se pudo guardar'));
        }
      })
      .catch(err => {
        console.error(err);
        alert('Error al guardar cambios');
      });
  });

});
