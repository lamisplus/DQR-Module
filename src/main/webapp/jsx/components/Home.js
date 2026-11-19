import React, {useState, Fragment } from "react";
import { Row, Col, Card,  Tab, Tabs, } from "react-bootstrap";
import ListGrid from './List/index'
import Cbs from './Cbs/index'
// import PatientVaccinatedLIst from './Patient/PatientVaccinatedLIst'
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
        {/* mountOnEnter: the CBS screens only load (and call the server) once their tab is opened */}
        <Tabs defaultActiveKey="dqr" id="dqr-main-tabs" className="dqr-main-tabs mb-3 mt-4" mountOnEnter>
          <Tab eventKey="dqr" title="Data Quality Report">
            <ListGrid />
          </Tab>
          <Tab eventKey="cbs" title="HIV Case-Based Surveillance">
            <Cbs />
          </Tab>
        </Tabs>
    </Fragment>
  );
};

export default Home;
