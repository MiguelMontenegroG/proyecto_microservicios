// Script para generar usuario admin con contraseña correcta
// Conectarse a MongoDB y ejecutar:

db.usuarios.deleteMany({});

db.usuarios.insertOne({
  username: "admin",
  password: "$2a$10$rKOxQlPg5ZJHv9k4mNqO7.vK8GHZL9pqRJxF7RbqXhKzJ2vN8qP2S",  // "password123" en BCrypt
  email: "admin@empresa.com",
  nombreCompleto: "Administrador del Sistema",
  rol: "ADMIN",
  activo: true,
  fechaCreacion: new Date(),
  fechaModificacion: new Date()
});

db.usuarios.insertOne({
  username: "user",
  password: "$2a$10$rKOxQlPg5ZJHv9k4mNqO7.vK8GHZL9pqRJxF7RbqXhKzJ2vN8qP2S",  // "password123" en BCrypt
  email: "user@empresa.com",
  nombreCompleto: "Usuario Básico",
  rol: "USER",
  activo: true,
  fechaCreacion: new Date(),
  fechaModificacion: new Date()
});

print("Usuarios creados exitosamente!");
print("Admin: admin / password123");
print("User: user / password123");
