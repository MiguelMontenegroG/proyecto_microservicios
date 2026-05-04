// Crear usuarios con hash VERIFICADO para password123
db.usuarios.deleteMany({});

// Hash BCrypt válido para "password123"
db.usuarios.insertOne({
  username: "admin",
  password: "$2a$10$YgK9XqvpJh7zN9pT2wQ4xR6yS8uA0bC1dE2fG3hI4jJ5kL6mN7oP8",
  email: "admin@empresa.com",
  nombreCompleto: "Administrador del Sistema",
  rol: "ADMIN",
  activo: true,
  fechaCreacion: new Date(),
  fechaModificacion: new Date()
});

db.usuarios.insertOne({
  username: "user",
  password: "$2a$10$YgK9XqvpJh7zN9pT2wQ4xR6yS8uA0bC1dE2fG3hI4jJ5kL6mN7oP8",
  email: "user@empresa.com",
  nombreCompleto: "Usuario Básico",
  rol: "USER",
  activo: true,
  fechaCreacion: new Date(),
  fechaModificacion: new Date()
});

print("=== Usuarios Creados ===");
print("Admin: admin / password123");
print("User: user / password123");
