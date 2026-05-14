Feature: Seguridad y control de acceso
  Como administrador del sistema
  Quiero verificar el control de acceso basado en roles
  Para asegurar que el sistema tiene una correcta gestion de permisos

  Background:
    Given que el sistema esta desplegado y operativo

  Scenario: Acceso denegado sin token de autenticacion
    When consulto la lista de empleados sin token de autenticacion
    Then la respuesta debe tener codigo 403

  Scenario: Acceso con token invalido
    When uso un token invalido para acceder a empleados
    Then la respuesta debe tener codigo 403

  Scenario: Usuario ADMIN puede crear empleados
    Given que estoy autenticado como "ADMIN"
    And que existe un departamento "IT" con nombre "Tecnologia"
    When registro un nuevo empleado con datos:
      | id   | nombre  | apellido | email                       | departamentoId |
      | SEC1 | Juan    | Perez    | juan.perez@empresa.com       | IT            |
    Then la respuesta debe tener codigo 201

  Scenario: Usuario USER no puede crear empleados
    Given que estoy autenticado como "USER"
    And que existe un departamento "IT" con nombre "Tecnologia"
    When registro un nuevo empleado con datos:
      | id   | nombre  | apellido | email                       | departamentoId |
      | SEC2 | Ana     | Garcia   | ana.garcia@empresa.com       | IT            |
    Then la respuesta debe tener codigo 403
