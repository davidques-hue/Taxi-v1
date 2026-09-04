# Taxi Caja v1.1

Aplicación Android para registrar ingresos y egresos de taxi, turnos y mantenimiento del móvil.

## Cambios v1.1
- Teclado visible con números grandes y negros.
- Orden de calculadora: 7-8-9 / 4-5-6 / 1-2-3 / C-0-000.
- Botón ⌫ para borrar el último dígito.
- INGRESAR guarda de inmediato, actualiza los totales y vuelve a $0.
- Aviso temporal no bloqueante con opción DESHACER durante 4 segundos.
- Los montos rápidos solo cargan la cifra; para guardarla se debe pulsar INGRESAR.
- Gradle 8.13 en GitHub Actions.
- Cache de la firma debug para facilitar futuras actualizaciones desde GitHub Actions.

## Importante al instalar v1.1
La v1.0 que se compiló antes de activar la conservación de la firma puede tener una firma diferente. Si Android no permite instalar v1.1 encima de v1.0, desinstala v1.0 una sola vez e instala v1.1. Desde v1.1, las compilaciones futuras intentarán conservar la misma firma mediante la cache de GitHub Actions.
