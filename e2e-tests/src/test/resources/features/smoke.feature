Feature: Verificacion del sistema
  Como usuario del sistema
  Quiero verificar que el sistema este desplegado y funcional
  Para asegurar la disponibilidad del sistema

  Scenario: El sistema responde correctamente
    Given que el sistema esta desplegado y operativo
    Then la respuesta debe tener codigo 200
