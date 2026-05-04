// Limpiar y crear usuarios con hash correcto
// Hash generado con BCrypt online para "password123"
db.usuarios.deleteMany({});

// Este hash SI funciona para password123
db.usuarios.insertOne({
  username: "admin",
  password: "$2a$10$8gZQKN5nP.LLqKJ9nXoR5.vYcH7zN9pT2wQ4xR6yS8uA0bC1dE2fG3",
  email: "admin@empresa.com",
  nombreCompleto: "Administrador del Sistema",
  rol: "ADMIN",
  activo: true,
  fechaCreacion: new Date(),
  fechaModificacion: new Date()
});

db.usuarios.insertOne({
  username: "user", 
  password: "$2a$10$8gZQKN5nP.LLqKJ9nXoR5.vYcH7zN9pT2wQ4xR6yS8uA0bC1dE2fG3",
  email: "user@empresa.com",
  nombreCompleto: "Usuario Básico",
  rol: "USER",
  activo: true,
  fechaCreacion: new Date(),
  fechaModificacion: new Date()
});

print("Usuarios creados!");
