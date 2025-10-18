let editMode = false;
let changedSites = new Map();
let originalStates = new Map();
let isSelecting = false;
let selectedSites = new Set();

// Inicializar contadores al cargar la página
document.addEventListener("DOMContentLoaded", function () {
  updateAvailableCount();

  // Guardar estados originales
  document.querySelectorAll(".site-box").forEach((box) => {
    const siteId = box.getAttribute("data-site-id");
    const status = box.getAttribute("data-status");
    originalStates.set(siteId, status);
  });

  // Event listeners para selección múltiple
  document.addEventListener('mousedown', (e) => {
    if (editMode && e.target.closest('.site-box')) {
      isSelecting = true;
    }
  });

  document.addEventListener('mouseup', () => {
    if (isSelecting) {
      isSelecting = false;
      console.log('Sitios seleccionados:', Array.from(selectedSites));
    }
  });

  document.addEventListener('mouseleave', () => {
    isSelecting = false;
  });
});

function clearAllSelections() {
  document.querySelectorAll('.site-box').forEach(box => {
    box.classList.remove('ring-4', 'ring-blue-500', 'ring-opacity-50');
  });
}

function toggleEditMode() {
  editMode = !editMode;
  const btn = document.getElementById("editModeBtn");
  const message = document.getElementById("editModeMessage");
  const actionsPanel = document.getElementById("actionsPanel");

  if (editMode) {
    btn.innerHTML = `
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                    </svg>
                    Desactivar Modo Edición
                `;
    btn.classList.remove("bg-primary-500", "hover:bg-primary-600");
    btn.classList.add("bg-danger-500", "hover:bg-danger-600");
    message.classList.remove("hidden");
    actionsPanel.classList.remove("hidden");
  } else {
    btn.innerHTML = `
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                    </svg>
                    Activar Modo Edición
                `;
    btn.classList.remove("bg-danger-500", "hover:bg-danger-600");
    btn.classList.add("bg-primary-500", "hover:bg-primary-600");
    message.classList.add("hidden");
    actionsPanel.classList.add("hidden");
  }
}

function toggleSiteStatus(element) {
  if (!editMode) return;

  const siteId = element.getAttribute("data-site-id");
  
  // Lógica de selección (tanto para click como para arrastre)
  if (selectedSites.has(siteId)) {
    // Si ya está seleccionado, deseleccionar
    selectedSites.delete(siteId);
    element.classList.remove('ring-4', 'ring-blue-500', 'ring-opacity-50');
  } else {
    // Si no está seleccionado, seleccionar
    selectedSites.add(siteId);
    element.classList.add('ring-4', 'ring-blue-500', 'ring-opacity-50');
  }
  
  // Actualizar panel de acciones de selección
  updateSelectionPanel();
}

function updateSiteVisual(element, status) {
  // Remover todas las clases de color
  element.classList.remove(
    "bg-success-500",
    "border-success-600",
    "hover:bg-success-600",
    "bg-danger-500",
    "border-danger-600",
    "hover:bg-danger-600",
    "bg-warning-500",
    "border-warning-600",
    "hover:bg-warning-600",
    "bg-gray-400",
    "border-gray-500",
    "hover:bg-gray-500"
  );

  // Aplicar nuevas clases según estado
  if (status === "available") {
    element.classList.add(
      "bg-success-500",
      "border-success-600",
      "hover:bg-success-600"
    );
  } else if (status === "occupied") {
    element.classList.add(
      "bg-danger-500",
      "border-danger-600",
      "hover:bg-danger-600"
    );
  } else if (status === "reserved") {
    element.classList.add(
      "bg-warning-500",
      "border-warning-600",
      "hover:bg-warning-600"
    );
  } else {
    element.classList.add(
      "bg-gray-400",
      "border-gray-500",
      "hover:bg-gray-500"
    );
  }

  // Actualizar texto del estado
  const statusSpan = element.querySelector(".text-\\[8px\\]");
  if (statusSpan) {
    statusSpan.textContent = status;
  }
}

function updateAvailableCount() {
  const available = document.querySelectorAll(
    '.site-box[data-status="available"]'
  ).length;
  document.getElementById("availableCount").textContent = available;
}

function updateChangedCount() {
  document.getElementById("changedCount").textContent = changedSites.size;
}

function updateSelectionPanel() {
  const selectionPanel = document.getElementById("selectionPanel");
  const selectedCountSpan = document.getElementById("selectedCount");
  
  if (selectedSites.size > 0) {
    selectionPanel.classList.remove("hidden");
    selectedCountSpan.textContent = selectedSites.size;
  } else {
    selectionPanel.classList.add("hidden");
  }
}

function applyStatusToSelected(newStatus) {
  if (selectedSites.size === 0) {
    alert("No hay sitios seleccionados");
    return;
  }

  // Aplicar el nuevo estado a todos los sitios seleccionados
  selectedSites.forEach(siteId => {
    const element = document.querySelector(`[data-site-id="${siteId}"]`);
    if (element) {
      // Actualizar visualmente
      updateSiteVisual(element, newStatus);
      
      // Actualizar atributo
      element.setAttribute("data-status", newStatus);
      
      // Guardar cambio
      changedSites.set(siteId, newStatus);
      
      // Quitar selección visual
      element.classList.remove('ring-4', 'ring-blue-500', 'ring-opacity-50');
    }
  });

  // Limpiar selección
  selectedSites.clear();
  updateSelectionPanel();
  
  // Actualizar contadores
  updateAvailableCount();
  updateChangedCount();
  
  console.log(`${selectedSites.size} sitios cambiados a: ${newStatus}`);
}

function cancelSelection() {
  // Limpiar selecciones visuales
  clearAllSelections();
  selectedSites.clear();
  updateSelectionPanel();
}

function cancelChanges() {
  // Revertir todos los cambios
  changedSites.forEach((newStatus, siteId) => {
    const element = document.querySelector(`[data-site-id="${siteId}"]`);
    const originalStatus = originalStates.get(siteId);
    if (element && originalStatus) {
      updateSiteVisual(element, originalStatus);
      element.setAttribute("data-status", originalStatus);
    }
  });

  // Limpiar cambios
  changedSites.clear();
  updateAvailableCount();
  updateChangedCount();

  // Desactivar modo edición
  if (editMode) {
    toggleEditMode();
  }
}

function saveChanges() {
  if (changedSites.size === 0) {
    alert("No hay cambios para guardar");
    return;
  }

  // Preparar datos para enviar
  const changes = Array.from(changedSites.entries()).map(
    ([siteId, status]) => ({
      siteId: siteId,
      status: status,
    })
  );

  // Enviar al servidor
  fetch("/sites/batch-update", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(changes),
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.success) {
        alert("Cambios guardados exitosamente");

        // Actualizar estados originales
        changedSites.forEach((status, siteId) => {
          originalStates.set(siteId, status);
        });

        // Limpiar cambios
        changedSites.clear();
        updateChangedCount();

        // Desactivar modo edición
        if (editMode) {
          toggleEditMode();
        }
      } else {
        alert("Error al guardar cambios: " + data.message);
      }
    })
    .catch((error) => {
      console.error("Error:", error);
      alert("Error al guardar cambios");
    });
}
