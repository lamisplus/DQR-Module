import React, { useEffect, useState } from "react";
import axios from "axios";
import MaterialTable from 'material-table';
import { token, url as baseUrl } from "../../../../api";
import {  Table } from "reactstrap";
import { makeStyles } from "@material-ui/core/styles";
import { Card, CardContent } from "@material-ui/core";
import "semantic-ui-css/semantic.min.css";
import {  Button, } from "semantic-ui-react";

import { forwardRef } from 'react';
import "react-toastify/dist/ReactToastify.css";
import "react-widgets/dist/css/react-widgets.css";
import AddBox from '@material-ui/icons/AddBox';
import ArrowUpward from '@material-ui/icons/ArrowUpward';
import Check from '@material-ui/icons/Check';
import ChevronLeft from '@material-ui/icons/ChevronLeft';
import ChevronRight from '@material-ui/icons/ChevronRight';
import Clear from '@material-ui/icons/Clear';
import DeleteOutline from '@material-ui/icons/DeleteOutline';
import Edit from '@material-ui/icons/Edit';
import FilterList from '@material-ui/icons/FilterList';
import FirstPage from '@material-ui/icons/FirstPage';
import LastPage from '@material-ui/icons/LastPage';
import Remove from '@material-ui/icons/Remove';
import SaveAlt from '@material-ui/icons/SaveAlt';
import Search from '@material-ui/icons/Search';
import ViewColumn from '@material-ui/icons/ViewColumn';

const tableIcons = {
  Add: forwardRef((props, ref) => <AddBox {...props} ref={ref} />),
  Check: forwardRef((props, ref) => <Check {...props} ref={ref} />),
  Clear: forwardRef((props, ref) => <Clear {...props} ref={ref} />),
  Delete: forwardRef((props, ref) => <DeleteOutline {...props} ref={ref} />),
  DetailPanel: forwardRef((props, ref) => <ChevronRight {...props} ref={ref} />),
  Edit: forwardRef((props, ref) => <Edit {...props} ref={ref} />),
  Export: forwardRef((props, ref) => <SaveAlt {...props} ref={ref} />),
  Filter: forwardRef((props, ref) => <FilterList {...props} ref={ref} />),
  FirstPage: forwardRef((props, ref) => <FirstPage {...props} ref={ref} />),
  LastPage: forwardRef((props, ref) => <LastPage {...props} ref={ref} />),
  NextPage: forwardRef((props, ref) => <ChevronRight {...props} ref={ref} />),
  PreviousPage: forwardRef((props, ref) => <ChevronLeft {...props} ref={ref} />),
  ResetSearch: forwardRef((props, ref) => <Clear {...props} ref={ref} />),
  Search: forwardRef((props, ref) => <Search {...props} ref={ref} />),
  SortArrow: forwardRef((props, ref) => <ArrowUpward {...props} ref={ref} />),
  ThirdStateCheck: forwardRef((props, ref) => <Remove {...props} ref={ref} />),
  ViewColumn: forwardRef((props, ref) => <ViewColumn {...props} ref={ref} />)
  };



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


    const Clinicals = (props) => {
    const classes = useStyles();
    const [clinicals, setClinical] = useState({});
    const [showPatientDetail, setPatientDetail] = useState(false);
    const [getHeaderInfo, setGetHeaderInfo] = useState("");
    const [clinicalsPatientsView, setClinicalsPatientsView] = useState([])

    useEffect(() => {
        loadClinical();
      }, []);
       
      const loadClinical = () => {
        axios
          .get(`${baseUrl}dqr/data-consistency-summary`, {
            headers: { Authorization: `Bearer ${token}` },
          })
          .then((response) => {
            setClinical(response.data);
          })
          .catch((error) => {
            console.log(error);
          });
      };

  
    const viewDetail =(headerTitle,patientDemoObj)=>{
    setPatientDetail(true)
    setGetHeaderInfo(headerTitle)
    const patientDemo =patientDemoObj
    axios
          .get(`${baseUrl}dqr/patient-consistency?indicator=${patientDemo}`, {
            headers: { Authorization: `Bearer ${token}` },
          })
          .then((response) => {
            setClinicalsPatientsView(response.data);
            //console.log(response.data[0])
          })
          .catch((error) => {
            console.log(error);
          });
    }
    const BackToList=()=> {
      setPatientDetail(false)
    }


    return (
        <>
           
            <Card className={classes.root}>
                <CardContent>
                    <h3>Clinicals</h3>
                    <div className="col-xl-12 col-lg-12">
                    {!showPatientDetail &&(<>
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
                                    Proportion of all active patients without documented target group
                                </td>
                                 <td>{clinicals[0]?.targNumerator}</td>
                                 <td>{clinicals[0]?.targDenominator}</td>
                                 <td>{clinicals[0]?.targPerformance} %</td>
                                <td>
                                    <div>
                                        <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients without documented target group", "dataCon0" )}> View</p>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    2
                                </th>
                                <td>
                                    Proportion of all active patients with a documented care entry point
                                </td>
                                <td>{clinicals[0]?.entryNumerator}</td>
                                <td>{clinicals[0]?.entryDenominator}</td>
                                <td>{clinicals[0]?.entryPerformance} %</td>
                                <td>
                                <div>
                                        <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with a documented care entry point", "dataCon1" )}> View</p>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    3
                                </th>
                                <td>
                                    Proportion of all active patients with documented abnormal weight of 121 and above
                                </td>
                                <td>{clinicals[0]?.adultWeightNumerator}</td>
                                <td>{clinicals[0]?.adultWeightDenominator}</td>
                                <td>{clinicals[0]?.adultWeightPerformance} %</td>
                                <td>
                                    <div>
                                        <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with documented abnormal weight of 121 and above", "dataCon2" )}> View</p>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    4
                                </th>
                                <td>
                                    Proportions of all active paediatric patients age 0 – 14 on ART that had documented weight of 61 and above
                                </td>
                                <td>{clinicals[0]?.peadWeightNumerator}</td>
                                <td>{clinicals[0]?.peadWeightDenominator}</td>
                                <td>{clinicals[0]?.peadWeightPerformance} %</td>
                                <td>
                                    <div>
                                        <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportions of all active paediatric patients age 0 – 14 on ART that had documented weight of 61 and above", "dataCon3" )}> View</p>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    5
                                </th>
                                <td>
                                    Proportion of all active female patient 12 year and above with a documented pregnancy status
                                </td>
                                <td>{clinicals[0]?.pregNumerator}</td>
                                <td>{clinicals[0]?.pregDenominator}</td>
                                <td>{clinicals[0]?.pregPerformance} %</td>
                                <td>
                                <div>
                                        <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active female patient 12 year and above with a documented pregnancy status", "dataCon4" )}> View</p>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    6
                                </th>
                                <td>
                                    Proportion of all active patients with an ART start date on or before the current calendar date
                                </td>
                               <td>{clinicals[0]?.artDateLessTodayNumerator}</td>
                               <td>{clinicals[0]?.artDateLessTodayDenominator}</td>
                               <td>{clinicals[0]?.artDateLessTodayPerformance} %</td>
                                <td>
                                    <div>
                                        <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with an ART start date on or before the current calendar date", "DataCon5" )}> View</p>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    7
                                </th>
                                <td>
                                    Proportion of all active patients with an ART start date on or before last clinic visit date
                                </td>
                                <td>{clinicals[0]?.artEqClinicNumerator}</td>
                                <td>{clinicals[0]?.artEqClinicDenominator}</td>
                                <td>{clinicals[0]?.artEqClinicPerformance} %</td>
                                <td>
                                    <div>
                                        <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with an ART start date on or before last clinic visit date", "DataCon6" )}> View</p>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    8
                                </th>
                                <td>
                                    Proportion of all active patients with an ART start date on or before last drug pickup date
                                </td>
                                <td>{clinicals[0]?.artEqLastPickupNumerator}</td>
                                <td>{clinicals[0]?.artEqLastPickupDenominator}</td>
                                <td>{clinicals[0]?.artEqLastPickupPerformance} %</td>
                                <td>
                                    <div>
                                        <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with an ART start date on or before last drug pickup date", "DataCon7" )}> View</p>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                              <th scope="row">
                                  9
                              </th>
                              <td>
                                  Proportion of all active patients with last drug pickup date on or after first confirmed HIV date
                              </td>
                              <td>{clinicals[0]?.lgreaterConfNumerator}</td>
                              <td>{clinicals[0]?.lgreaterConfDenominator}</td>
                              <td>{clinicals[0]?.lgreaterConfPerformance} %</td>
                              <td>
                                    <div>
                                        <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with last drug pickup date on or after first confirmed HIV date", "DataCon8" )}> View</p>
                                    </div>
                              </td>
                              </tr>
                              <tr>
                             <th scope="row">
                                 10
                             </th>
                             <td>
                                 Proportion of all active patients with ART start date before transfer-in date
                             </td>
                             <td>{clinicals[0]?.artGreaterTransNumerator}</td>
                             <td>{clinicals[0]?.artGreaterTransDenominator}</td>
                             <td>{clinicals[0]?.artGreaterTransPerformance} %</td>
                             <td>
                                    <div>
                                        <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with ART start date before transfer-in date", "DataCon9" )}> View</p>
                                    </div>
                             </td>
                             </tr>

                                <tr>
                                <th scope="row">
                                    11
                                </th>
                                <td>
                                    Proportion of all active patients with Last drug pickup date after date of birth
                                </td>
                                <td>{clinicals[0]?.lstPickGreaterDObNumerator}</td>
                                <td>{clinicals[0]?.lstPickGreaterDObDenominator}</td>
                                <td>{clinicals[0]?.lstPickGreaterDObPerformance} %</td>
                                <td>
                                    <div>
                                        <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with Last drug pickup date after date of birth", "DataCon10" )}> View</p>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                            <th scope="row">
                                12
                            </th>
                            <td>
                                Proportion of all active patients Newly initiated on ART (TX_NEW) in the quarter but has previous quarter drug pickup date
                            </td>
                            <td> N/A</td>
                            <td>N/A</td>
                            <td> %</td>
                            <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients Newly initiated on ART (TX_NEW) in the quarter but has previous quarter drug pickup date", "" )}> View</p>
                                </div>
                            </td>
                            </tr>
                            <tr>
                            <th scope="row">
                                13
                            </th>
                            <td>
                                Proportion of all active patients with Last drug pickup date on or after transferred in date
                            </td>
                            <td>{clinicals[0]?.ldrugPickHighNumerator}</td>
                            <td>{clinicals[0]?.ldrugPickHighDenominator}</td>
                            <td>{clinicals[0]?.ldrugPickHighPerformance} %</td>
                            <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with Last drug pickup date on or after transferred in date", "DataCon11" )}> View</p>
                                </div>
                            </td>
                            </tr>
                            <tr>
                             <th scope="row">
                                 14
                             </th>
                             <td>
                                 Proportion of all active patients with Last drug pickup date on or before current calendar date
                             </td>
                             <td>{clinicals[0]?.clinicPickLessTodayNumerator}</td>
                             <td>{clinicals[0]?.clinicPickLessTodayDenominator}</td>
                             <td>{clinicals[0]?.clinicPickLessTodayPerformance} %</td>
                             <td>
                                    <div>
                                        <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with Last drug pickup date on or before current calendar date", "DataCon12" )}> View</p>
                                    </div>
                             </td>
                             </tr>
<tr>
                             <th scope="row">
                                 15
                             </th>
                             <td>
                                 Proportion of all active patients with Last clinic visit date on or before current calendar date
                             </td>
                             <td>{clinicals[0]?.clinicPickLessTodayNumerator}</td>
                             <td>{clinicals[0]?.clinicPickLessTodayDenominator}</td>
                             <td>{clinicals[0]?.clinicPickLessTodayPerformance} %</td>
                             <td>
                                     <div>
                                        <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with Last clinic visit date on or before current calendar date", "DataCon13" )}> View</p>
                                    </div>
                             </td>
                             </tr>
<tr>
                             <th scope="row">
                                 16
                             </th>
                             <td>
                                 Proportion of all active patients with Date of VL result after the date of VL sample collection
                             </td>
                             <td></td>
                             <td></td>
                             <td> %</td>
                             <td>
                                <div>
                                        <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with Date of VL result after the date of VL sample collection", "" )}> View</p>
                                </div>
                             </td>
                             </tr>
<tr>
                             <th scope="row">
                                 17
                             </th>
                             <td>
                                 Proportion of new patients (TX_NEW) with CD4 count
                             </td>
                             <td></td>
                             <td></td>
                             <td> %</td>
                             <td>
                                    <div>
                                        <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of new patients (TX_NEW) with CD4 count", "" )}> View</p>
                                    </div>
                             </td>
                             </tr>
                            </tbody>
                        </Table>
                        </>)}
                        {showPatientDetail &&(<>
                        <Button
                        variant="contained"
                        style={{backgroundColor:"#014d88", }}
                        className=" float-right mr-1"
                        //startIcon={<FaUserPlus />}
                        onClick={BackToList}
                        >
                        <span style={{ textTransform: "capitalize", color:"#fff" }}> {"<<"} Back </span>
                        </Button>
                        <br/>
                        <br/> 
                        <MaterialTable
                            icons={tableIcons}
                            title={getHeaderInfo}
                            columns={[

                              {
                                title: "Hospital Number",
                                field: "hospitalNumber",
                              },
                              { title: "Sex ", field: "sex", filtering: false },
                              { title: "Date Of Birth", field: "dob", filtering: false },
                              { title: "Status", field: "status", filtering: false },

                            ]}
                            data={ clinicalsPatientsView.map((row) => ({
                              //Id: manager.id,
                              hospitalNumber: row.hospitalNumber,
                              sex: row.sex,
                              dob: row.dateOfBirth,
                              status:row.status

                            }))}

                            options={{
                              headerStyle: {
                                backgroundColor: "#014d88",
                                color: "#fff",
                              },
                              searchFieldStyle: {
                                width : '200%',
                                margingLeft: '250px',
                              },
                              filtering: false,
                              exportButton: true,
                              searchFieldAlignment: 'left',
                              pageSizeOptions:[10,20,100],
                              pageSize:10,
                              debounceInterval: 400
                            }}
                        />
            </>)}
                    </div>
                </CardContent>
            </Card>

        </>
    );
};

export default Clinicals