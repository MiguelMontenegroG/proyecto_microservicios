// Script para crear usuario admin
db.usuarios.insertOne({
  username: 'admin',
  password: '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  email: 'admin@empresa.com',
  rol: 'ADMIN',
  activo: true,
  fechaCreacion: new Date(),
  fechaModificacion: new Date()
});

print('Admin creado exitosamente');
print('Credenciales: admin / password123');
