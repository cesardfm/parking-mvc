// Variables globales
let selectedSite = null;
let sitesData = {}; // Almacenará los datos de sitios por nivel

// Inicialización cuando el DOM está listo
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Inicializando formulario de tickets...');
    
    const levelSelect = document.getElementById('levelSelect');
    
    // Cargar datos de sitios desde el HTML (inyectados por Thymeleaf)
    loadSitesDataFromPage();
    
    if (levelSelect) {
        levelSelect.addEventListener('change', handleLevelChange);
        console.log('✅ Event listener agregado al selector de niveles');
    } else {
        console.error('❌ No se encontró el elemento levelSelect');
    }
});

/**
 * Carga los datos de sitios desde elementos hidden en el HTML
 */
function loadSitesDataFromPage() {
    console.log('📥 Cargando datos de sitios desde el HTML...');
    
    const sitesDataElements = document.querySelectorAll('[data-level-sites]');
    console.log(`📊 Encontrados ${sitesDataElements.length} elementos con datos de sitios`);
    
    sitesDataElements.forEach((element, index) => {
        const levelId = element.getAttribute('data-level-id');
        const sitesJson = element.getAttribute('data-sites');
        const rows = parseInt(element.getAttribute('data-rows'));
        const columns = parseInt(element.getAttribute('data-columns'));
        
        console.log(`\n📦 Nivel ${levelId}:`);
        console.log(`  - Filas: ${rows}`);
        console.log(`  - Columnas: ${columns}`);
        console.log(`  - JSON Raw:`, sitesJson ? sitesJson.substring(0, 100) + '...' : 'null');
        
        try {
            const sites = JSON.parse(sitesJson);
            console.log(`  - Sitios parseados: ${sites.length}`);
            
            sitesData[levelId] = {
                sites: sites,
                rows: rows,
                columns: columns
            };
            
            console.log(`  ✅ Nivel ${levelId} cargado correctamente con ${sites.length} sitios`);
        } catch (e) {
            console.error(`  ❌ Error parseando JSON para nivel ${levelId}:`, e);
            console.error(`  JSON que falló:`, sitesJson);
        }
    });
    
    console.log('\n📊 Resumen de datos cargados:');
    console.log('sitesData:', sitesData);
    console.log(`Total niveles cargados: ${Object.keys(sitesData).length}`);
}

/**
 * Maneja el cambio de nivel seleccionado
 */
function handleLevelChange(event) {
    const levelId = event.target.value;
    const sitesContainer = document.getElementById('sitesContainer');
    const sitesGrid = document.getElementById('sitesGrid');
    
    console.log(`\n🔄 Cambio de nivel detectado: ${levelId}`);
    
    // Limpiar selección previa
    selectedSite = null;
    document.getElementById('siteId').value = '';
    document.getElementById('selectedSiteInfo').classList.add('hidden');
    
    if (!levelId) {
        console.log('⚠️ No se seleccionó ningún nivel');
        sitesContainer.classList.add('hidden');
        return;
    }
    
    // Obtener datos del nivel
    const levelData = sitesData[levelId];
    
    console.log('🔍 Buscando datos para nivel:', levelId);
    console.log('Datos encontrados:', levelData);
    
    if (!levelData) {
        console.error('❌ No se encontraron datos para el nivel:', levelId);
        console.log('Niveles disponibles:', Object.keys(sitesData));
        
        sitesGrid.innerHTML = `
            <div class="col-span-full p-6 bg-red-50 border border-red-200 rounded-lg">
                <p class="text-red-700 text-center">
                    <strong>Error:</strong> No se encontraron datos para este nivel.
                    <br><small>Nivel ID: ${levelId}</small>
                </p>
            </div>
        `;
        sitesContainer.classList.remove('hidden');
        return;
    }
    
    console.log(`✅ Datos del nivel encontrados: ${levelData.sites.length} sitios, ${levelData.rows}x${levelData.columns}`);
    
    // Mostrar contenedor y renderizar grilla
    sitesContainer.classList.remove('hidden');
    renderSitesGrid(levelData.sites, levelData.rows, levelData.columns);
}

/**
 * Renderiza la grilla de sitios
 */
function renderSitesGrid(sites, rows, columns) {
    console.log(`\n🎨 Renderizando grilla: ${rows}x${columns}, Total sitios: ${sites.length}`);
    
    const sitesGrid = document.getElementById('sitesGrid');
    
    // Configurar el grid según las columnas del nivel
    sitesGrid.style.gridTemplateColumns = `repeat(${columns}, minmax(50px, 60px))`;
    sitesGrid.className = 'grid gap-2 mb-4 justify-center';
    
    // Crear un mapa de sitios por posición
    const sitesMap = {};
    sites.forEach(site => {
        const key = `${site.posY}-${site.posX}`;
        sitesMap[key] = site;
    });
    
    console.log('🗺️ Mapa de sitios creado:', sitesMap);
    
    // Generar HTML para cada sitio
    let html = '';
    let availableCount = 0;
    
    for (let row = 0; row < rows; row++) {
        for (let col = 0; col < columns; col++) {
            const key = `${row}-${col}`;
            const site = sitesMap[key];
            
            if (site) {
                const isAvailable = site.status === 'available';
                if (isAvailable) availableCount++;
                
                const bgColor = isAvailable 
                    ? 'bg-success-500 border-success-600 hover:bg-success-600' 
                    : 'bg-gray-400 border-gray-500';
                const cursor = isAvailable ? 'cursor-pointer' : 'cursor-not-allowed';
                
                html += `
                    <div 
                        class="site-box aspect-square rounded-lg border-2 flex flex-col items-center justify-center transition-all ${cursor} select-none ${bgColor}"
                        data-site-id="${site.id}"
                        data-status="${site.status}"
                        data-row="${site.posY}"
                        data-col="${site.posX}"
                        ${isAvailable ? `onclick="selectSite(${site.id}, '${site.posY}-${site.posX}')"` : ''}
                    >
                        <span class="text-white text-xs font-bold">${site.posY}-${site.posX}</span>
                        ${!isAvailable ? '<span class="text-white text-[10px] opacity-80">Ocupado</span>' : ''}
                    </div>
                `;
            }
        }
    }
    
    sitesGrid.innerHTML = html;
    console.log(`✅ Grilla renderizada con ${availableCount} sitios disponibles`);
}

/**
 * Selecciona un sitio
 */
function selectSite(siteId, siteNumber) {
    console.log(`✅ Sitio seleccionado: ${siteNumber} (ID: ${siteId})`);
    
    // Remover selección previa
    const previousSelected = document.querySelector('.site-box.ring-4');
    if (previousSelected) {
        previousSelected.classList.remove('ring-4', 'ring-primary-500', 'scale-110');
    }
    
    // Seleccionar nuevo sitio
    const siteElement = document.querySelector(`[data-site-id="${siteId}"]`);
    if (siteElement) {
        siteElement.classList.add('ring-4', 'ring-primary-500', 'scale-110');
    }
    
    // Actualizar variables
    selectedSite = siteId;
    document.getElementById('siteId').value = siteId;
    
    // Mostrar información del sitio seleccionado
    const selectedSiteInfo = document.getElementById('selectedSiteInfo');
    const selectedSiteNumber = document.getElementById('selectedSiteNumber');
    
    selectedSiteNumber.textContent = siteNumber;
    selectedSiteInfo.classList.remove('hidden');
    
    // Scroll suave hacia la información
    selectedSiteInfo.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

/**
 * Validación del formulario antes de enviar
 */
document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('form');
    
    if (form) {
        form.addEventListener('submit', function(event) {
            const siteId = document.getElementById('siteId').value;
            const levelSelect = document.getElementById('levelSelect');
            
            // Solo validar si estamos creando un ticket nuevo
            const isCreating = !document.getElementById('paid');
            
            if (isCreating && levelSelect && levelSelect.value && !siteId) {
                event.preventDefault();
                console.warn('⚠️ Formulario bloqueado: No se ha seleccionado un sitio');
                alert('Por favor, selecciona un sitio antes de continuar.');
                
                // Hacer scroll al contenedor de sitios
                const sitesContainer = document.getElementById('sitesContainer');
                if (sitesContainer) {
                    sitesContainer.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
        });
    }
});