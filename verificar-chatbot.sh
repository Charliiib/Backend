#!/bin/bash

# 🧪 Script de Verificación para Chatbot Railway + Vercel
# Ejecutar después del deploy para verificar funcionamiento

echo "🚀 INICIANDO VERIFICACIÓN DEL CHATBOT"
echo "======================================"

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# URLs
BACKEND_URL="https://backend-production-4d5a.up.railway.app"
FRONTEND_URL="https://frontend-pi-jet-42.vercel.app"
API_KEY="tu_google_ai_api_key_aqui" # ⚠️ REEMPLAZAR con tu API key real

print_status() {
    local status=$1
    local message=$2
    if [ "$status" = "OK" ]; then
        echo -e "${GREEN}✅ $message${NC}"
    elif [ "$status" = "ERROR" ]; then
        echo -e "${RED}❌ $message${NC}"
    elif [ "$status" = "WARNING" ]; then
        echo -e "${YELLOW}⚠️  $message${NC}"
    elif [ "$status" = "INFO" ]; then
        echo -e "${BLUE}ℹ️  $message${NC}"
    fi
}

echo ""
echo "🔍 VERIFICACIÓN 1: Health Check del Backend"
echo "============================================"

health_response=$(curl -s -o /dev/null -w "%{http_code}" "${BACKEND_URL}/actuator/health")
if [ "$health_response" = "200" ]; then
    print_status "OK" "Backend responde correctamente (HTTP $health_response)"

    # Mostrar detalles del health check
    health_data=$(curl -s "${BACKEND_URL}/actuator/health")
    echo -e "${BLUE}📊 Detalles del Health Check:${NC}"
    echo "$health_data" | jq '.' 2>/dev/null || echo "$health_data"
else
    print_status "ERROR" "Backend no responde correctamente (HTTP $health_response)"
fi

echo ""
echo "🔍 VERIFICACIÓN 2: Test CORS Preflight"
echo "======================================"

cors_response=$(curl -s -i "${BACKEND_URL}/api/chatbot/consulta-stream?mensaje=test" \
  -H "Origin: ${FRONTEND_URL}" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization" \
  -X OPTIONS)

if echo "$cors_response" | grep -q "Access-Control-Allow-Origin"; then
    print_status "OK" "Headers CORS presentes en respuesta"
    echo -e "${BLUE}📋 Headers CORS encontrados:${NC}"
    echo "$cors_response" | grep "Access-Control"
else
    print_status "ERROR" "Headers CORS NO encontrados en respuesta"
fi

echo ""
echo "🔍 VERIFICACIÓN 3: Test SSE Endpoint (sin streaming)"
echo "===================================================="

sse_response=$(curl -s -o /dev/null -w "%{http_code}" "${BACKEND_URL}/api/chatbot/consulta-stream?mensaje=pizza" \
  -H "Origin: ${FRONTEND_URL}" \
  -H "Authorization: Bearer test")

if [ "$sse_response" = "200" ]; then
    print_status "OK" "Endpoint SSE responde correctamente (HTTP $sse_response)"
else
    print_status "ERROR" "Endpoint SSE no responde correctamente (HTTP $sse_response)"
fi

echo ""
echo "🔍 VERIFICACIÓN 4: Test API POST (Fallback)"
echo "==========================================="

api_response=$(curl -s -X POST "${BACKEND_URL}/api/chatbot/solo-receta" \
  -H "Content-Type: application/json" \
  -H "Origin: ${FRONTEND_URL}" \
  -d '{"mensaje":"como hacer una pizza"}')

if echo "$api_response" | grep -q "INGREDIENTES\|Instrucciones\|receta"; then
    print_status "OK" "API POST funciona correctamente"
    echo -e "${BLUE}📝 Respuesta de ejemplo:${NC}"
    echo "$api_response" | head -c 200
    echo "..."
else
    print_status "ERROR" "API POST no responde correctamente"
    echo -e "${RED}📄 Respuesta recibida:${NC}"
    echo "$api_response"
fi

echo ""
echo "🔍 VERIFICACIÓN 5: Frontend Configuración"
echo "========================================"

# Verificar si el frontend está accesible
frontend_response=$(curl -s -o /dev/null -w "%{http_code}" "${FRONTEND_URL}")
if [ "$frontend_response" = "200" ]; then
    print_status "OK" "Frontend accesible (HTTP $frontend_response)"
else
    print_status "ERROR" "Frontend no accesible (HTTP $frontend_response)"
fi

echo ""
echo "🔍 VERIFICACIÓN 6: Variables de Entorno"
echo "======================================"

print_status "INFO" "Verificar en Railway Dashboard:"
echo "  • GOOGLE_AI_API_KEY configurado"
echo "  • PORT configurado (8080)"

print_status "INFO" "Verificar en Vercel Dashboard:"
echo "  • REACT_APP_API_URL configurado: ${BACKEND_URL}"

echo ""
echo "🧪 VERIFICACIÓN 7: Test de Conectividad"
echo "======================================="

# Test de conectividad básica
ping -c 1 backend-production-4d5a.up.railway.app > /dev/null 2>&1
if [ $? -eq 0 ]; then
    print_status "OK" "Conectividad a backend: OK"
else
    print_status "WARNING" "Conectividad a backend: Falla (puede ser normal en algunos entornos)"
fi

echo ""
echo "📋 RESUMEN DE VERIFICACIÓN"
echo "=========================="

# Verificar todo y mostrar resumen final
all_checks=0
passed_checks=0

# Contar checks que pasaron
if [ "$health_response" = "200" ]; then ((passed_checks++)); fi
((all_checks++))

if echo "$cors_response" | grep -q "Access-Control-Allow-Origin"; then ((passed_checks++)); fi
((all_checks++))

if [ "$sse_response" = "200" ]; then ((passed_checks++)); fi
((all_checks++))

if [ "$frontend_response" = "200" ]; then ((passed_checks++)); fi
((all_checks++))

echo -e "${BLUE}📊 Puntuación: ${passed_checks}/${all_checks} checks pasaron${NC}"

if [ "$passed_checks" -eq "$all_checks" ]; then
    print_status "OK" "🎉 TODOS LOS CHECKS PASARON - Tu chatbot debería funcionar correctamente!"
elif [ "$passed_checks" -ge 3 ]; then
    print_status "WARNING" "⚠️  La mayoría de checks pasaron, el chatbot debería funcionar con problemas menores"
else
    print_status "ERROR" "❌ Múltiples checks fallaron - Revisar configuración antes de usar el chatbot"
fi

echo ""
echo "🔧 ACCIONES POSTERIORES"
echo "======================"
echo "1. Si algún check falló, revisar la configuración en los dashboards"
echo "2. Esperar 2-3 minutos para que los cambios se propaguen"
echo "3. Probar el chatbot en el frontend en vivo"
echo "4. Verificar logs en Railway Dashboard si persisten errores"

echo ""
echo -e "${BLUE}🔗 Enlaces útiles:${NC}"
echo "• Backend Health: ${BACKEND_URL}/actuator/health"
echo "• Frontend: ${FRONTEND_URL}"
echo "• Railway Dashboard: https://railway.app/dashboard"
echo "• Vercel Dashboard: https://vercel.com/dashboard"

echo ""
echo "✅ VERIFICACIÓN COMPLETADA"
echo "========================="