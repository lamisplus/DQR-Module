import React, {useState, Fragment } from "react";
import { Row, Col, Card,  Tab, Tabs, } from "react-bootstrap";
import PatientList from './Patient/PatientList'
import PatientVaccinatedLIst from './Patient/PatientVaccinatedLIst'
import { Link } from 'react-router-dom'
import Button from '@material-ui/core/Button';
import { FaUserPlus } from "react-icons/fa";

const divStyle = {
  borderRadius: "2px",
  fontSize: 14,
};

const Home = () => {


  return (
    <Fragment>  
        <div>Home</div>
    </Fragment>
  );
};

export default Home;
