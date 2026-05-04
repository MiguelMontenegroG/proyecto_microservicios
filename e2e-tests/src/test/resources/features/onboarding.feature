Feature: Registro de empleados
  Como administrador del sistema
  Quiero registrar nuevos empleados
  Para incorporar nuevos usuarios al sistema

  Background:
    Given que estoy autenticado como "ADMIN"
    And que existe un departamento "IT" con nombre "Tecnologia"

  Scenario: Registro exitoso de empleado
    When registro un nuevo empleado con datos:
      | id     | nombre  | apellido | email                          | departamentoId |
      | E001   | Carlos  | Mendoza  | carlos.mendoza@empresa.com     | IT            |
    Then la respuesta debe tener codigo 201
    And eventualmente el empleado con ID "E001" debe existir

  Scenario: Verificar creacion de credenciales via evento
    Given que registro un nuevo empleado con datos:
      | id     | nombre  | apellido | email                          | departamentoId |
      | E002   | Laura   | Martinez | laura.martinez@empresa.com     | IT            |
    Then eventualmente el servicio de autenticacion debe haber creado un usuario para "E002"

  Scenario: Verificar notificacion de registro
    Given que registro un nuevo empleado con datos:
      | id     | nombre  | apellido | email                          | departamentoId |
      | E003   | Pedro   | Sanchez  | pedro.sanchez@empresa.com     | IT            |
    Then eventualmente se debe haber generado una notificacion de tipo "BIENVENIDA" para "E003"

  Scenario: Nuevo empleado puede solicitar recuperacion de contrasena
    Given que registro un nuevo empleado con datos:
      | id     | nombre  | apellido | email                          | departamentoId |
      | E004   | Sofia   | Ramirez  | sofia.ramirez@empresa.com     | IT            |
    And eventualmente el empleado con ID "E004" debe existir
    And eventualmente el servicio de autenticacion debe haber creado un usuario para "E004"
    When intento recuperar la contrasena del empleado con email "sofia.ramirez@empresa.com"
    Then la respuesta debe tener codigo 200
    And la respuesta debe indicar que la recuperacion fue exitosa

  Scenario: Registro con departamento inexistente
    When registro un nuevo empleado con datos:
      | id     | nombre  | apellido | email                      | departamentoId |
      | E005   | Maria   | Lopez    | maria.lopez@empresa.com   | DEP999        |
    Then la respuesta debe tener codigo 400

  Scenario: Registro con datos faltantes
    When registro un nuevo empleado con datos:
      | id     | nombre  | departamentoId |
      | E006   | Jorge   | IT            |
    Then la respuesta debe tener codigo 400

