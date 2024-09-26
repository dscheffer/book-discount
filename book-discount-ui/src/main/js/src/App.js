import './App.css';
import CalculatePriceForm from './CalculatePriceForm';
import { Container } from '@mui/material';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';

function App() {
  return (
    <div className="App">
      <AppBar position="sticky">
        <Toolbar>
          <Typography variant="h6" component="div">
            Buch-Preisrechner
          </Typography>
        </Toolbar>
      </AppBar>
      <Container>
        <CalculatePriceForm />
      </Container>
    </div>
  );
}

export default App;
