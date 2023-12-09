import React, { useEffect, useState } from "react";
import axios from "axios";
import MaterialTable from 'material-table';
import { token, url as baseUrl } from "../../../../api";
import { Form, Table } from "reactstrap";
import { makeStyles } from "@material-ui/core/styles";
import { Card, CardContent } from "@material-ui/core";
import "semantic-ui-css/semantic.min.css";
import { Dropdown, Button, Menu, Icon } from "semantic-ui-react";

import ErrorIcon from "@mui/icons-material/Error";
import { FiUploadCloud } from "react-icons/fi";
import { forwardRef } from 'react';
import "react-toastify/dist/ReactToastify.css";
import "react-widgets/dist/css/react-widgets.css";
import CloudUpload from '@material-ui/icons/CloudUpload';
import moment from "moment";
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


    const PrEP = (props) => {
    const classes = useStyles();
    const [prep, setPrep] = useState({});
    const [facilities, setFacilities] = useState([]);
    const [showPatientDetail, setPatientDetail] = useState(false);
    const [getHeaderInfo, setGetHeaderInfo] = useState("");
    const [prepPatientsView, setPrepPatientsView] = useState([])
  
      useEffect(() => {
        loadPrep();
      }, []);
  
      
    const loadPrep = () => {
      axios
        .get(`${baseUrl}dqr/prep-summary`, {
          headers: { Authorization: `Bearer ${token}` },
        })
        .then((response) => {
          setPrep(response.data);
          console.log(response.data)
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
          .get(`${baseUrl}dqr/biometric-error?indicator=${patientDemo}`, {
            headers: { Authorization: `Bearer ${token}` },
          })
          .then((response) => {
            setPrepPatientsView(response.data);
            //console.log(response.data[0])
          })
          .catch((error) => {
            console.log(error);
          });
    }
    const BackToList=()=> {
      setPatientDetail(false)
    }
  
    const Facilities = () => {
      axios
        .get(`${baseUrl}account`, {
          headers: { Authorization: `Bearer ${token}` },
        })
        .then((response) => {
          setFacilities(response.data.currentOrganisationUnitId);
        })
        .catch((error) => {
          console.log(error);
        });
    };
  
    const loadprep = () => {
      axios
        .get(`${baseUrl}dqr/prep-summary`, {
          headers: { Authorization: `Bearer ${token}` },
        })
        .then((response) => {
          setPrep(response.data);
          console.log(response.data)
        })
        .catch((error) => {
          console.log(error);
        });
    };
  
    useEffect(() => {
      Facilities();
      loadprep();
    }, []);

    return (
        <>
           
            <Card className={classes.root}>
                <CardContent>
                    <h3>PrEP</h3>
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
                                    Proportion of eligible patients who were offered PrEP 
                                </td>
                                <td>{prep[0]?.pofferredNumerator}</td>
                                <td>{prep[0]?.pofferedDenominator}</td>
                                <td>{prep[0]?.pofferredPerformance} %</td>
                                <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of eligible patients who were offered PrEP", "" )}> View</p>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    2
                                </th>
                                <td>
                                    Proportion of eligible patients who accepted PrEP 
                                </td>
                                <td>{prep[0]?.pofferredNumerator}</td>
                                <td>{prep[0]?.pacceptedDenominator}</td>
                                <td>{prep[0]?.pacceptedPerformance} %</td>
                                <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of eligible patients who accepted PrEP ", "" )}> View</p>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    3
                                </th>
                                <td>
                                    Proportion of patients who accepted PrEP who are initiated on PrEP
                                </td>
                                <td>{prep[0]?.penrollNumerator}</td>
                                <td>{prep[0]?.penrollDenominator}</td>
                                <td>{prep[0]?.penrollPerformance} %</td>
                                <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of patients who accepted PrEP who are initiated on PrEP ", "" )}> View</p>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    4
                                </th>
                                <td>
                                    Proportion of patients who were initiated on PrEP and had urinalysis done
                                </td>
                                <td>{prep[0]?.penrolledPrepUrinaNumerator}</td>
                                <td>{prep[0]?.penrolledPrepUrinaDenominator}</td>
                                <td>{prep[0]?.penrolledPrepUrinaPerformance} %</td>
                                <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of patients who were initiated on PrEP and had urinalysis done", "" )}> View</p>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    5
                                </th>
                                <td>
                                    Proportion of patients with date of current urinalysis {">"} date of enrolment
                                </td>
                                <td>{prep[0]?.purinaGreaterEnrollNumerator}</td>
                                <td>{prep[0]?.purinaGreaterEnrollDenominator}</td>
                                <td>{prep[0]?.purinaGreaterEnrollPerformance} %</td>
                                <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of patients with date of current urinalysis > date of enrolment", "" )}> View</p>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    6
                                </th>
                                <td>
                                    Proportion of patients with date of current urinalysis {">"} date of HIV status
                                </td>
                                <td>{prep[0]?.purinaGreaterStatusDateNumerator}</td>
                                <td>{prep[0]?.purinaGreaterStatusDateDenominator}</td>
                                <td>{prep[0]?.purinaGreaterStatusDatePerformance} %</td>
                                <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of patients with date of current urinalysis > date of HIV status", "" )}> View</p>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    7
                                </th>
                                <td>
                                    Proportion of patients with date of registration Less than date of PrEP commencement
                                </td>
                                <td>{prep[0]?.commencedNumerator}</td>
                                <td>{prep[0]?.commencedDenominator}</td>
                                <td>{prep[0]?.commencedPerformance} %</td>
                                <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of patients with date of registration Less than date of PrEP commencement", "" )}> View</p>
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
                            data={ prepPatientsView.map((row) => ({
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

export default PrEP