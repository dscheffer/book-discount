import { useState } from 'react';
import './CalculatePriceForm.css';
import useSWR from 'swr';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import { Box, Card, Container, Typography } from '@mui/material';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';

function calculatePrice(event, setCalculateDisabled, quantities, setPriceResult, setDialogOpen) {
    event.preventDefault();
    setCalculateDisabled(true);
    fetch('/api/price/calculate', {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(quantities)
    }).then(result => result.text())
    .then(setPriceResult)
    .then(() => setDialogOpen(true))
    .finally(() => setCalculateDisabled(false));
}

function onQuantityChange(bookId, quantity, setQuantities) {
    const newQuantity = {bookId, quantity: !quantity ? 0 : quantity};

    setQuantities((oldValues) => {
        const index = oldValues.map(o => o.bookId).indexOf(bookId);
        if (index > -1) {
            oldValues.splice(index, 1);
        }
        return [...oldValues, newQuantity];
    });
}

function renderBooksForm(data, calculateDisabled, setCalculateDisabled, quantities, setQuantities, setPriceResult, setDialogOpen) {
    return (
        <Card className="book-form" sx={{
            mt: 5
        }}>
            <form onSubmit={event => calculatePrice(event, setCalculateDisabled, quantities, setPriceResult, setDialogOpen)}>
                {data && data.map((b, i) => (
                    <div key={b.id}>
                        <Typography className="book-label" variant="h5">{b.name + ' – ' + b.price + '€'}</Typography>
                        <TextField 
                            className="quantity-field" 
                            variant="filled"
                            label="Menge"
                            type="number" 
                            min="0" 
                            step="1" 
                            disabled={calculateDisabled} 
                            onChange={(event) => onQuantityChange(b.id,  event.target.value, setQuantities)}
                        />
                    </div>
                ))}
                <div>
                    <Button variant="contained" type="submit" disabled={calculateDisabled}>Calculate price</Button>
                </div>
            </form>
        </Card>
    )
}

const fetcher = (...args) => fetch(...args).then((res) => res.json());

export default function CalculatePriceForm() {
    const {data, isLoading} = useSWR('/api/books', fetcher)
    const [calculateDisabled, setCalculateDisabled] = useState(false);

    const [quantities, setQuantities] = useState([]);
    const [dialogOpen, setDialogOpen] = useState(false);
    const [priceResult, setPriceResult] = useState(0);

    return (
        <Box>
            {isLoading 
                ? (<p>Loading...</p>)
                : renderBooksForm(
                    data, 
                    calculateDisabled, 
                    setCalculateDisabled, 
                    quantities, 
                    setQuantities,
                    setPriceResult,
                    setDialogOpen
                )
            }
            <Dialog onClose={() => setDialogOpen(false)} open={dialogOpen}
                fullWidth={true}>
                <DialogTitle>Preisrechner Ergebnis</DialogTitle>
                <Container sx= {{
                    height: 75
                }}>
                    <Typography variant="body1">{priceResult}€</Typography>
                </Container>
            </Dialog>
        </Box>
    );
}