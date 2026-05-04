Feature: Desvinculacion de empleados
  Como administrador del sistema
  Quiero eliminar empleados del sistema
  Para gestionar la baja de usuarios

  Background:
    Given que estoy autenticado como "ADMIN"
    And que existe un departamento "IT" con nombre "Tecnologia"
    And que existe un empleado registrado con:
      | id     | nombre  | apellido | email                         | departamentoId |
      | OFF001 | Elena   | Torres   | elena.torres@empresa.com     | IT            |

  Scenario: Desvinculacion completa de empleado
    When elimino al empleado con ID "OFF001"
    Then la respuesta debe tener codigo 204
    And eventualmente el empleado con ID "OFF001" no debe existir

  Scenario: Empleado desvinculado no puede hacer login
    Given que el empleado con ID "OFF001" ha sido desvinculado
    And eventualmente el empleado con ID "OFF001" no debe existir
    When intento hacer login con credenciales:
      | username | password |
      | elena.torres@empresa.com | password123 |
    Then la respuesta debe tener codigo 403

  Scenario: Recuperacion de contrasena falla para empleado desvinculado
    Given que el empleado con ID "OFF001" ha sido desvinculado
    And eventualmente el empleado con ID "OFF001" no debe existir
    When intento recuperar la contrasena del empleado con email "elena.torres@empresa.com"
    Then la respuesta debe tener codigo 403

