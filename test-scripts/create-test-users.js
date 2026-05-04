// Script para crear usuarios de prueba en auth-service
db.usuarios.deleteMany({});

// Hash BCrypt para "password123": $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
// Este es un hash estándar y ampliamente usado para pruebas

db.usuarios.insertOne({
  username: "admin",
  password: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
  email: "admin@empresa.com",
  nombreCompleto: "Administrador del Sistema",
  rol: "ADMIN",
  activo: true,
  fechaCreacion: new Date(),
  fechaModificacion: new Date()
});

db.usuarios.insertOne({
  username: "user",
  password: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
  email: "user@empresa.com",
  nombreCompleto: "Usuario Básico",
  rol: "USER",
  activo: true,
  fechaCreacion: new Date(),
  fechaModificacion: new Date()
});

print("=== Usuarios Creados Exitosamente ===");
print("Admin - username: admin, password: password123");
print("User - username: user, password: password123");
