import React from "react";
import {Form, Table } from "reactstrap";
import {makeStyles} from "@material-ui/core/styles";
import {Card, CardContent} from "@material-ui/core";

import "react-toastify/dist/ReactToastify.css";
import "react-widgets/dist/css/react-widgets.css";



const useStyles = makeStyles((theme) => ({
    card: {
        margin: theme.spacing(20),
        display: "flex",
        flexDirection: "column",
        alignItems: "center"
    },
    form: {
        width: "100%", // Fix IE 11 issue.
        marginTop: theme.spacing(3),
    },
    submit: {
        margin: theme.spacing(3, 0, 2),
    },
    cardBottom: {
        marginBottom: 20,
    },
    Select: {
        height: 45,
        width: 300,
    },
    button: {
        margin: theme.spacing(1),
    },
    root: {
        '& > *': {
            margin: theme.spacing(1)
        },
        "& .card-title":{
            color:'#fff',
            fontWeight:'bold'
        },
        "& .form-control":{
            borderRadius:'0.25rem',
            height:'41px'
        },
        "& .card-header:first-child": {
            borderRadius: "calc(0.25rem - 1px) calc(0.25rem - 1px) 0 0"
        },
        "& .dropdown-toggle::after": {
            display: " block !important"
        },
        "& select":{
            "-webkit-appearance": "listbox !important"
        },
        "& p":{
            color:'red'
        },
        "& label":{
            fontSize:'14px',
            color:'#014d88',
            fontWeight:'bold'
        }
    },
    demo: {
        backgroundColor: theme.palette.background.default,
    },
    inline: {
        display: "inline",
    },
    error:{
        color: '#f85032',
        fontSize: '12.8px'
    },
    success: {
        color: "#4BB543 ",
        fontSize: "11px",
    },
}));


const PrEP = (props) => {
    const classes = useStyles();


    return (
        <>
           
            <Card className={classes.root}>
                <CardContent>
                    <h3>PrEP</h3>
                    <div className="col-xl-12 col-lg-12">
                        <Table bordered>
                            <thead>
                            <tr>
                                <th>
                                    #
                                </th>
                                <th>
                                    Complete Variables
                                </th>
                                <th>
                                    Numerator
                                </th>
                                <th>
                                  Denominator
                                </th>
                                <th>
                                  Performance
                                </th>
                                <th>
                                    Action
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <th scope="row">
                                    1
                                </th>
                                <td>
                                    Proportion of eligible patients who were offered PrEP 
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    2
                                </th>
                                <td>
                                    Proportion of eligible patients who accepted PrEP 
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    3
                                </th>
                                <td>
                                Proportion of patients who accepted PrEP who are initiated on PrEP
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    4
                                </th>
                                <td>
                                    Proportion of patients who were initiated on PrEP and had urinalysis done
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    5
                                </th>
                                <td>
                                    Proportion of patients with date of current urinalysis > date of enrolment
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    6
                                </th>
                                <td>
                                    Proportion of patients with date of current urinalysis {">"} date of HIV status
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    7
                                </th>
                                <td>
                                Proportion of patients with date of registration Less than date of PrEP commencement
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                                <td>
                                    
                                </td>
                            </tr>
                            

                            </tbody>
                        </Table>
                    </div>
                </CardContent>
            </Card>

        </>
    );
};

export default PrEP