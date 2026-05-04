const bcrypt = require('bcrypt');

const password = 'password123';
const saltRounds = 10;

bcrypt.hash(password, saltRounds).then(hash => {
    console.log('Hash generado para "' + password + '":');
    console.log(hash);
}).catch(err => {
    console.error('Error:', err);
});
