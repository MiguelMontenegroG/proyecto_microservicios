Feature: Desvinculacion de empleados
  Como administrador del sistema
  Quiero eliminar empleados del sistema
  Para gestionar la baja de usuarios

  Background:
    Given que estoy autenticado como "ADMIN"
    And que existe un departamento "IT" con nombre "Tecnologia"

  Scenario: Desvinculacion completa de empleado
    Given que existe un empleado registrado con:
      | id     | nombre  | apellido | email                         | departamentoId |
      | OFF001 | Elena   | Torres   | elena.torres@empresa.com      | IT            |
    When elimino al empleado con ID "OFF001"
    Then la respuesta debe tener codigo 204
    And eventualmente el empleado con ID "OFF001" no debe existir

  Scenario: Empleado desvinculado no puede hacer login
    Given que existe un empleado registrado con:
      | id     | nombre  | apellido | email                         | departamentoId |
      | OFF002 | Carlos  | Ruiz     | carlos.ruiz@empresa.com       | IT            |
    When elimino al empleado con ID "OFF002"
    And eventualmente el empleado con ID "OFF002" no debe existir
    When intento hacer login con credenciales:
      | username                  | password    |
      | carlos.ruiz@empresa.com   | password123 |
    Then la respuesta debe tener codigo 403

  Scenario: Recuperacion de contrasena falla para empleado desvinculado
    Given que existe un empleado registrado con:
      | id     | nombre  | apellido | email                         | departamentoId |
      | OFF003 | Lucia   | Gomez    | lucia.gomez@empresa.com       | IT            |
    When elimino al empleado con ID "OFF003"
    And eventualmente el empleado con ID "OFF003" no debe existir
    When intento recuperar la contrasena del empleado con email "lucia.gomez@empresa.com"
    Then la respuesta debe tener codigo 200
    And la respuesta debe indicar que la recuperacion fue exitosa
